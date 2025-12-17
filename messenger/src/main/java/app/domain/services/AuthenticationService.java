package app.domain.services;

import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import app.application.exceptions.BusinessException;
import app.application.exceptions.UnauthorizedException;
import app.domain.model.Employee;
import app.domain.model.auth.AuthCredentials;
import app.domain.model.auth.TokenResponse;
import app.domain.ports.AuthenticationPort;
import app.domain.ports.EmployeePort;
import app.infrastructure.security.TokenBlacklistService;

/**
 * Servicio de dominio para autenticación de usuarios.
 * 
 * Gestiona el proceso completo de autenticación incluyendo:
 * - Validación de credenciales (username y password)
 * - Verificación de existencia del usuario
 * - Comparación de contraseñas con hash BCrypt
 * - Migración automática de contraseñas planas a BCrypt
 * - Generación de tokens JWT para sesiones
 * - Refresh Token Rotation con blacklist en Redis
 * 
 * Seguridad:
 * - Refresh Token Rotation: cada uso de un refresh token genera uno nuevo
 * - Tokens usados se añaden a una blacklist en Redis
 * - Reutilización de token revocado indica posible robo (se rechaza)
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
    private TokenBlacklistService tokenBlacklistService;

    @Value("${jwt.expiration:1800000}")
    private long jwtExpiration;

    /**
     * Autentica un usuario y genera un token JWT.
     * 
     * Verifica las credenciales, migra contraseñas planas a BCrypt si es necesario,
     * y genera un token de sesión.
     * 
     * @param credentials Credenciales del usuario (username y password).
     * @return TokenResponse con el token JWT y rol del usuario.
     * @throws Exception Si el usuario no existe o la contraseña es incorrecta.
     */
    public TokenResponse authenticate(AuthCredentials credentials) throws Exception {
        logger.debug("Autenticando usuario: {}", credentials.getUserName());
        Employee employee = employeePort.findByUserName(credentials.getUserName());
        if (employee == null) {
            logger.warn("Usuario no encontrado: {}", credentials.getUserName());
            throw new BusinessException("Usuario no encontrado");
        }
        if (!passwordEncoder.matches(credentials.getPassword(), employee.getPassword())) {
            if (!isPasswordEncoded(employee.getPassword())
                    && credentials.getPassword().equals(employee.getPassword())) {
                String encoded = passwordEncoder.encode(credentials.getPassword());
                employee.setPassword(encoded);
                employeePort.save(employee);
                logger.info("Contraseña migrada a BCrypt para usuario: {}", credentials.getUserName());
            } else {
                logger.warn("Contraseña incorrecta para usuario: {}", credentials.getUserName());
                throw new BusinessException("Contrasena incorrecta");
            }
        }
        logger.info("Usuario autenticado exitosamente: {} (rol: {})", credentials.getUserName(), employee.getRole());
        TokenResponse response = authenticationPort.authenticate(credentials, String.valueOf(employee.getRole()));
        String refreshToken = authenticationPort.generateRefreshToken(credentials);
        response.setRefreshToken(refreshToken);
        return response;
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
     * Renueva el token de acceso usando un refresh token.
     * 
     * Implementa Refresh Token Rotation:
     * 1. Verifica que el token no esté en la blacklist (reutilización = posible
     * robo)
     * 2. Valida el token actual
     * 3. Genera nuevos tokens (access + refresh)
     * 4. Añade el token usado a la blacklist
     * 
     * @param refreshToken Token de refresco.
     * @return Nueva respuesta con tokens actualizados.
     * @throws UnauthorizedException Si el token está en la blacklist.
     * @throws BusinessException     Si el token es inválido o el usuario no existe.
     */
    public TokenResponse refreshToken(String refreshToken) throws Exception {
        // ========== REFRESH TOKEN ROTATION: Verificar blacklist ==========
        if (tokenBlacklistService.isBlacklisted(refreshToken)) {
            logger.error("SEGURIDAD: Intento de reutilización de refresh token revocado");
            throw new UnauthorizedException("Token revocado. Por seguridad, vuelva a iniciar sesión.");
        }

        if (!authenticationPort.validateRefreshToken(refreshToken)) {
            throw new BusinessException("Refresh token inválido o expirado");
        }

        String username = authenticationPort.extractUsername(refreshToken);
        Employee employee = employeePort.findByUserName(username);
        if (employee == null) {
            throw new BusinessException("Usuario no encontrado");
        }

        AuthCredentials credentials = new AuthCredentials();
        credentials.setUserName(username);

        logger.info("Refrescando token para usuario: {}", username);

        // Generamos nuevos tokens
        TokenResponse response = authenticationPort.authenticate(credentials, String.valueOf(employee.getRole()));

        // ========== REFRESH TOKEN ROTATION: Generar nuevo y blacklistear anterior
        // ==========
        String newRefreshToken = authenticationPort.generateRefreshToken(credentials);
        response.setRefreshToken(newRefreshToken);

        // Añadir el token usado a la blacklist
        // TTL = tiempo de expiración del refresh token (24x el access token)
        long refreshTokenTtlSeconds = (jwtExpiration * 24) / 1000;
        tokenBlacklistService.addToBlacklist(refreshToken, refreshTokenTtlSeconds);
        logger.debug("Refresh token anterior añadido a blacklist");

        return response;
    }
}