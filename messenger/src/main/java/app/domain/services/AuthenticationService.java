package app.domain.services;

import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import app.application.exceptions.BusinessException;
import app.domain.model.Employee;
import app.domain.model.auth.AuthCredentials;
import app.domain.model.auth.TokenResponse;
import app.domain.ports.AuthenticationPort;
import app.domain.ports.EmployeePort;

/**
 * Servicio de dominio para autenticación de usuarios.
 * 
 * Gestiona el proceso completo de autenticación incluyendo:
 * Validación de credenciales (username y password)
 * Verificación de existencia del usuario
 * Comparación de contraseñas con hash BCrypt
 * Migración automática de contraseñas planas a BCrypt
 * Generación de tokens JWT para sesiones
 * Gestión de Refresh Tokens para renovación de acceso
 * 
 * Incluye lógica de retrocompatibilidad para migrar contraseñas
 * almacenadas en texto plano a formato BCrypt de manera transparente.
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
     * @param refreshToken Token de refresco.
     * @return Nueva respuesta con tokens actualizados.
     * @throws Exception Si el token es inválido.
     */
    public TokenResponse refreshToken(String refreshToken) throws Exception {
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
        // Opcional: Rotación de refresh token (generar uno nuevo también)
        // Por ahora mantenemos el mismo refresh token o generamos uno nuevo según
        // política.
        // Vamos a generar uno nuevo para mayor seguridad (Refresh Token Rotation).
        String newRefreshToken = authenticationPort.generateRefreshToken(credentials);
        response.setRefreshToken(newRefreshToken);

        return response;
    }
}