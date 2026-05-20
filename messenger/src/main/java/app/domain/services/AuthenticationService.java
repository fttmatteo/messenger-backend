package app.domain.services;

import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import app.domain.exception.BusinessException;
import app.domain.exception.UnauthorizedException;
import app.domain.model.Employee;
import app.domain.model.auth.AuthCredentials;
import app.domain.model.auth.TokenResponse;
import app.domain.ports.AuthenticationPort;
import app.domain.ports.EmployeePort;
import app.domain.util.LogSanitizer;

/**
 * Servicio de autenticación y gestión de tokens JWT.
 */
@Service
public class AuthenticationService {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);

    private static final Pattern BCRYPT_PATTERN = Pattern.compile("\\A\\$2[ayb]\\$\\d\\d\\$[./A-Za-z0-9]{53}\\z");

    @Autowired
    private AuthenticationPort authenticationPort;
    @Autowired
    private EmployeePort employeePort;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private app.domain.ports.TokenBlacklistPort tokenBlacklistService;

    @Value("${jwt.expiration:1800000}")
    private long jwtExpiration;

    /**
     * Autentica un usuario verificando credenciales y generando tokens.
     * Si la contraseña es correcta pero no está encriptada, la actualiza
     * automáticamente.
     */
    public TokenResponse authenticate(AuthCredentials credentials) throws Exception {
        Employee employee = employeePort.findByDocument(credentials.getDocument());
        if (employee == null) {
            logger.warn("[Seguridad] Intento de login fallido: documento {} no existe", 
                    LogSanitizer.maskDocument(credentials.getDocument()));
            throw new BusinessException("Credenciales inválidas");
        }
        if (!passwordEncoder.matches(credentials.getPassword(), employee.getPassword())) {
            if (!isPasswordEncoded(employee.getPassword())
                    && credentials.getPassword().equals(employee.getPassword())) {
                logger.info("[Seguridad] Actualizando contraseña a BCrypt para empleado ID: {}", employee.getIdEmployee());
                String encoded = passwordEncoder.encode(credentials.getPassword());
                employee.setPassword(encoded);
                employeePort.save(employee);
            } else {
                logger.warn("[Seguridad] Intento de login fallido: contraseña incorrecta para documento {}", 
                        LogSanitizer.maskDocument(credentials.getDocument()));
                throw new BusinessException("Credenciales inválidas");
            }
        }
        TokenResponse response = authenticationPort.authenticate(credentials, String.valueOf(employee.getRole()),
                employee.getIdEmployee());
        String refreshToken = authenticationPort.generateRefreshToken(credentials);
        response.setRefreshToken(refreshToken);

        logger.info("[Seguridad] Login exitoso para documento {} con rol {}", 
                LogSanitizer.maskDocument(employee.getDocument()), employee.getRole());
        return response;
    }

    public boolean validateToken(String token) {
        return authenticationPort.validateToken(token);
    }

    public String extractUsername(String token) {
        return authenticationPort.extractUsername(token);
    }

    public String extractRole(String token) {
        return authenticationPort.extractRole(token);
    }

    /**
     * Genera un token de corta duración para WebSocket.
     */
    public String generateWsToken(String username, String role) {
        // Obtenemos el empleado para tener su ID (opcional pero útil)
        Employee employee = employeePort.findByDocument(Long.parseLong(username));
        Long userId = (employee != null) ? employee.getIdEmployee() : null;

        return authenticationPort.generateShortLivedToken(username, role, userId, 60000); // 60 segundos
    }

    private boolean isPasswordEncoded(String storedPassword) {
        if (storedPassword == null) {
            return false;
        }
        String normalized = storedPassword.startsWith("{bcrypt}")
                ? storedPassword.substring("{bcrypt}".length())
                : storedPassword;
        return BCRYPT_PATTERN.matcher(normalized).matches();
    }

    /**
     * Renueva el token de acceso utilizando un refresh token válido.
     * Invalida el refresh token usado y genera uno nuevo (Rotación de Refresh
     * Tokens).
     */
    public TokenResponse refreshToken(String refreshToken) throws Exception {
        if (tokenBlacklistService.isBlacklisted(refreshToken)) {
            logger.warn("[Seguridad] Intento de refresco con token en lista negra: {}", 
                    LogSanitizer.maskToken(refreshToken));
            throw new UnauthorizedException("Token revocado. Por seguridad, vuelva a iniciar sesión.");
        }

        if (!authenticationPort.validateRefreshToken(refreshToken)) {
            logger.warn("[Seguridad] Intento de refresco con token inválido o expirado");
            throw new UnauthorizedException("Refresh token inválido o expirado");
        }

        String documentStr = authenticationPort.extractUsername(refreshToken);
        Long document = Long.parseLong(documentStr);
        Employee employee = employeePort.findByDocument(document);
        if (employee == null) {
            logger.warn("[Seguridad] Intento de refresco fallido: empleado con documento {} no existe", 
                    LogSanitizer.maskDocument(document));
            throw new BusinessException("Credenciales inválidas");
        }

        AuthCredentials credentials = new AuthCredentials();
        credentials.setDocument(document);

        TokenResponse response = authenticationPort.authenticate(credentials, String.valueOf(employee.getRole()),
                employee.getIdEmployee());

        String newRefreshToken = authenticationPort.generateRefreshToken(credentials);
        response.setRefreshToken(newRefreshToken);

        long refreshTokenTtlSeconds = (jwtExpiration * 24) / 1000;
        tokenBlacklistService.addToBlacklist(refreshToken, refreshTokenTtlSeconds);

        logger.info("[Seguridad] Token refrescado exitosamente para documento {}", 
                LogSanitizer.maskDocument(document));
        return response;
    }
}