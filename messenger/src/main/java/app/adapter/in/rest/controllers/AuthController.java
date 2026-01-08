package app.adapter.in.rest.controllers;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import app.application.usecase.LoginUseCase;
import app.application.usecase.RefreshTokenUseCase;
import app.domain.model.auth.AuthCredentials;
import app.domain.model.auth.LoginResponse;
import app.domain.model.auth.TokenResponse;
import app.domain.services.LoginRateLimitService;
import app.infrastructure.audit.AuditableAction;
import app.infrastructure.logging.LogSanitizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controlador REST para autenticación y gestión de tokens.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private RefreshTokenUseCase refreshTokenUseCase;
    @Autowired
    private LoginUseCase loginUseCase;
    @Autowired
    private LoginRateLimitService rateLimitService;

    @Value("${jwt.expiration:1800000}")
    private long accessTokenExpiration;
    
    @Value("${app.cookie.secure:false}")
    private boolean cookieSecure;

    /**
     * Inicia sesión de usuario y genera tokens de acceso.
     * Los tokens se envían en cookies HttpOnly por seguridad.
     */
    @PostMapping("/login")
    @AuditableAction(action = "LOGIN", description = "Inicio de sesión de usuario")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody AuthCredentials credentials,
            HttpServletResponse response) {
        
        Long document = credentials.getDocument();
        logger.info("Solicitud de login recibida para documento: {}",
            LogSanitizer.maskDocument(document));
        
        // Verificar si la cuenta está bloqueada por demasiados intentos fallidos
        if (rateLimitService.isBlocked(document)) {
            logger.warn("Login rechazado - documento bloqueado por rate limit: {}",
                LogSanitizer.maskDocument(document));
            
            LoginResponse errorResponse = new LoginResponse(
                null,
                "Demasiados intentos fallidos. Intenta de nuevo en 15 minutos.",
                null
            );
            return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS) // 429
                .body(errorResponse);
        }
        
        try {
            // Obtener tokens y datos del usuario
            LoginUseCase.LoginResult loginResult = loginUseCase.login(credentials);
            TokenResponse tokenResponse = loginResult.tokenResponse;
            
            // Limpiar intentos fallidos tras login exitoso
            rateLimitService.clearFailedAttempts(document);
            
            // Setear access token en cookie HttpOnly
            Cookie accessTokenCookie = createSecureCookie(
                "accessToken", 
                tokenResponse.getToken(),
                (int) (accessTokenExpiration / 1000), // Convertir a segundos
                "/"
            );
            response.addCookie(accessTokenCookie);
            
            // Setear refresh token en cookie HttpOnly separada
            Cookie refreshTokenCookie = createSecureCookie(
                "refreshToken",
                tokenResponse.getRefreshToken(),
                24 * 60 * 60, // 24 horas en segundos
                "/auth/refresh" // Solo accesible para endpoint de refresh
            );
            response.addCookie(refreshTokenCookie);
            
            // Retornar metadata con datos del usuario
            LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(
                loginResult.employee.getIdEmployee(),
                loginResult.employee.getFullName(),
                credentials.getDocument(),
                null  // dealershipName - puede obtenerse después si es necesario
            );
            
            LoginResponse loginResponse = new LoginResponse(
                tokenResponse.getRole(),
                "Login exitoso",
                userInfo
            );
            
            logger.info("Login exitoso para documento: {} con rol: {}",
                LogSanitizer.maskDocument(document), tokenResponse.getRole());
            
            return ResponseEntity.ok(loginResponse);
            
        } catch (app.domain.exception.BusinessException e) {
            // Registrar intento fallido
            int remainingAttempts = rateLimitService.recordFailedAttempt(document);
            
            logger.warn("Login fallido para documento: {}. Intentos restantes: {} - Error: {}",
                LogSanitizer.maskDocument(document), 
                remainingAttempts,
                e.getMessage());
            
            String errorMessage = remainingAttempts > 0
                ? String.format("Credenciales inválidas. Intentos restantes: %d", remainingAttempts)
                : "Cuenta bloqueada por demasiados intentos fallidos. Intenta de nuevo en 15 minutos.";
            
            LoginResponse errorResponse = new LoginResponse(
                null,
                errorMessage,
                null
            );
            
            int statusCode = remainingAttempts > 0 ? 401 : 429;
            return ResponseEntity
                .status(statusCode)
                .body(errorResponse);
        }
    }

    /**
     * Renueva el token de acceso utilizando un refresh token de cookie.
     */
    @PostMapping("/refresh")
    @AuditableAction(action = "TOKEN_REFRESH", description = "Renovación de token de acceso")
    public ResponseEntity<LoginResponse> refresh(
            HttpServletRequest request,
            HttpServletResponse response) {
        
        // Extraer refresh token de cookie
        String refreshToken = extractTokenFromCookie(request, "refreshToken");
        
        if (refreshToken == null) {
            logger.warn("Intento de refresh sin token en cookie");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        // Crear request object para use case
        app.domain.model.auth.RefreshTokenRequest refreshRequest = 
            new app.domain.model.auth.RefreshTokenRequest();
        refreshRequest.setRefreshToken(refreshToken);
        
        TokenResponse tokenResponse = refreshTokenUseCase.refreshToken(refreshRequest);
        
        // Setear nuevo access token en cookie
        Cookie accessTokenCookie = createSecureCookie(
            "accessToken",
            tokenResponse.getToken(),
            (int) (accessTokenExpiration / 1000),
            "/"
        );
        response.addCookie(accessTokenCookie);
        
        // Opcionalmente renovar refresh token también
        if (tokenResponse.getRefreshToken() != null) {
            Cookie refreshTokenCookie = createSecureCookie(
                "refreshToken",
                tokenResponse.getRefreshToken(),
                24 * 60 * 60,
                "/auth/refresh"
            );
            response.addCookie(refreshTokenCookie);
        }
        
        LoginResponse loginResponse = new LoginResponse(
            tokenResponse.getRole(),
            "Token renovado exitosamente",
            null);

        return ResponseEntity.ok(loginResponse);
    }

    /**
     * Cierra sesión y limpia las cookies de autenticación.
     */
    @PostMapping("/logout")
    @AuditableAction(action = "LOGOUT", description = "Cierre de sesión de usuario")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        
        // Limpiar cookie de access token
        Cookie accessTokenCookie = createSecureCookie("accessToken", "", 0, "/");
        response.addCookie(accessTokenCookie);
        
        // Limpiar cookie de refresh token
        Cookie refreshTokenCookie = createSecureCookie("refreshToken", "", 0, "/auth/refresh");
        response.addCookie(refreshTokenCookie);
        
        logger.info("Logout exitoso - cookies limpiadas");
        
        return ResponseEntity.ok().build();
    }

    /**
     * Crea una cookie segura con los flags apropiados.
     */
    private Cookie createSecureCookie(String name, String value, int maxAge, String path) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);  // No accesible desde JavaScript (previene XSS)
        cookie.setSecure(cookieSecure);    // Solo HTTPS (configurable por perfil)
        cookie.setPath(path);      // Scope del cookie
        cookie.setMaxAge(maxAge);  // Tiempo de vida en segundos
        // SameSite=None permite enviar la cookie en contextos cross-site (frontend y backend en dominios distintos)
        if (cookieSecure) {
            cookie.setAttribute("SameSite", "None");
        }
        return cookie;
    }

    /**
     * Extrae un token de las cookies del request.
     */
    private String extractTokenFromCookie(HttpServletRequest request, String cookieName) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}