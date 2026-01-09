package app.adapter.in.rest.controllers;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
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
        LogSanitizer.maskDocument(document);

        if (rateLimitService.isBlocked(document)) {
            logger.warn("Login rechazado - documento bloqueado por rate limit: {}",
                    LogSanitizer.maskDocument(document));

            LoginResponse errorResponse = new LoginResponse(
                    null,
                    "Demasiados intentos fallidos. Intenta de nuevo en 15 minutos.",
                    null);
            return ResponseEntity
                    .status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(errorResponse);
        }

        try {
            LoginUseCase.LoginResult loginResult = loginUseCase.login(credentials);
            TokenResponse tokenResponse = loginResult.tokenResponse;

            rateLimitService.clearFailedAttempts(document);

            Cookie accessTokenCookie = createSecureCookie(
                    "accessToken",
                    tokenResponse.getToken(),
                    (int) (accessTokenExpiration / 1000),
                    "/");
            response.addCookie(accessTokenCookie);

            Cookie refreshTokenCookie = createSecureCookie(
                    "refreshToken",
                    tokenResponse.getRefreshToken(),
                    24 * 60 * 60,
                    "/");
            response.addCookie(refreshTokenCookie);

            LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(
                    loginResult.employee.getIdEmployee(),
                    loginResult.employee.getFullName(),
                    credentials.getDocument(),
                    null);

            LoginResponse loginResponse = new LoginResponse(
                    tokenResponse.getRole(),
                    "Login exitoso",
                    userInfo);

            return ResponseEntity.ok(loginResponse);

        } catch (app.domain.exception.BusinessException e) {
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
                    null);

            int statusCode = remainingAttempts > 0 ? 401 : 429;
            return ResponseEntity
                    .status(statusCode)
                    .body(errorResponse);
        }
    }

    /**
     * Genera un token de corta duración para la conexión inicial de WebSocket.
     * Útil para navegadores (como Safari Mobile) que no envían cookies en el
     * handshake.
     */
    @PostMapping("/ws-token")
    @AuditableAction(action = "WS_TOKEN_GEN", description = "Generación de token temporal para WebSocket")
    public ResponseEntity<app.domain.model.auth.WsTokenResponse> getWsToken(HttpServletRequest request) {
        // El usuario ya debe estar autenticado por cookie para llegar aquí
        String accessToken = extractTokenFromCookie(request, "accessToken");
        if (accessToken == null || !loginUseCase.validateToken(accessToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String username = loginUseCase.extractUsername(accessToken);
        String role = loginUseCase.extractRole(accessToken);
        // Podemos extraer el ID si es necesario, por ahora enviamos null si no lo
        // tenemos fácil
        // O mejor: el JwtAdapter ya sabe extraerlo si ajustamos la interfaz o usamos
        // claims directos

        String wsToken = loginUseCase.generateWsToken(username, role);

        return ResponseEntity.ok(new app.domain.model.auth.WsTokenResponse(wsToken));
    }

    /**
     * Renueva el token de acceso utilizando un refresh token de cookie.
     */
    @PostMapping("/refresh")
    @AuditableAction(action = "TOKEN_REFRESH", description = "Renovación de token de acceso")
    public ResponseEntity<LoginResponse> refresh(
            HttpServletRequest request,
            HttpServletResponse response) {

        String refreshToken = extractTokenFromCookie(request, "refreshToken");

        if (refreshToken == null) {
            logger.warn("Intento de refresh sin token en cookie");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        app.domain.model.auth.RefreshTokenRequest refreshRequest = new app.domain.model.auth.RefreshTokenRequest();
        refreshRequest.setRefreshToken(refreshToken);

        TokenResponse tokenResponse = refreshTokenUseCase.refreshToken(refreshRequest);

        Cookie accessTokenCookie = createSecureCookie(
                "accessToken",
                tokenResponse.getToken(),
                (int) (accessTokenExpiration / 1000),
                "/");
        response.addCookie(accessTokenCookie);

        Cookie refreshTokenCookie = createSecureCookie(
                "refreshToken",
                tokenResponse.getRefreshToken(),
                24 * 60 * 60,
                "/");

        if (tokenResponse.getRefreshToken() != null) {
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

        // Limpiamos cookies en la ruta raíz (nueva configuración)
        Cookie accessTokenCookie = createSecureCookie("accessToken", "", 0, "/");
        response.addCookie(accessTokenCookie);

        Cookie refreshTokenCookie = createSecureCookie("refreshToken", "", 0, "/");
        response.addCookie(refreshTokenCookie);

        // Limpiamos cookies en la ruta antigua (por si quedaran residuos en el
        // navegador)
        Cookie oldRefreshCookie = createSecureCookie("refreshToken", "", 0, "/auth/refresh");
        response.addCookie(oldRefreshCookie);

        return ResponseEntity.ok().build();
    }

    /**
     * Crea una cookie segura con los flags apropiados.
     */
    private Cookie createSecureCookie(String name, String value, int maxAge, String path) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieSecure);
        cookie.setPath(path);
        cookie.setMaxAge(maxAge);
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
            List<String> potentialTokens = Arrays.stream(request.getCookies())
                    .filter(c -> cookieName.equals(c.getName()))
                    .map(Cookie::getValue)
                    .filter(v -> v != null && !v.trim().isEmpty())
                    .toList();

            for (String val : potentialTokens) {
                // Para accessToken, validamos
                if (cookieName.equals("accessToken") && loginUseCase.validateToken(val)) {
                    return val;
                }
                // Para refreshToken, no podemos validar aquí fácilmente,
                // pero si hay varias, al menos devolvemos una que no esté vacía
                if (cookieName.equals("refreshToken")) {
                    return val;
                }
            }
        }
        return null;
    }
}