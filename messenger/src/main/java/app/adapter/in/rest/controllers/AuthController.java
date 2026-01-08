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
import app.infrastructure.audit.AuditableAction;
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

    @Value("${jwt.expiration:1800000}")
    private long accessTokenExpiration;

    /**
     * Inicia sesión de usuario y genera tokens de acceso.
     * Los tokens se envían en cookies HttpOnly por seguridad.
     */
    @PostMapping("/login")
    @AuditableAction(action = "LOGIN", description = "Inicio de sesión de usuario")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody AuthCredentials credentials,
            HttpServletResponse response) throws Exception {
        
        logger.info("Solicitud de login recibida para documento: {}", credentials.getDocument());
        TokenResponse tokenResponse = loginUseCase.login(credentials);
        
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
        
        // Retornar solo metadata (NO los tokens)
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(
            null, // id
            null, // name
            credentials.getDocument(),
            null  // dealershipName
        );
        
        LoginResponse loginResponse = new LoginResponse(
            tokenResponse.getRole(),
            "Login exitoso",
            userInfo
        );
        
        logger.info("Login exitoso para documento: {} con rol: {}", 
            credentials.getDocument(), tokenResponse.getRole());
        
        return ResponseEntity.ok(loginResponse);
    }

    /**
     * Renueva el token de acceso utilizando un refresh token de cookie.
     */
    @PostMapping("/refresh")
    @AuditableAction(action = "TOKEN_REFRESH", description = "Renovación de token de acceso")
    public ResponseEntity<LoginResponse> refresh(
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        
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
        cookie.setSecure(true);    // Solo HTTPS (en producción)
        cookie.setPath(path);      // Scope del cookie
        cookie.setMaxAge(maxAge);  // Tiempo de vida en segundos
        // SameSite=None permite enviar la cookie en contextos cross-site (frontend y backend en dominios distintos)
        cookie.setAttribute("SameSite", "None");
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