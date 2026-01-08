package app.application.usecase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import app.domain.model.auth.AuthCredentials;
import app.domain.model.auth.TokenResponse;
import app.domain.model.Employee;
import app.domain.services.AuthenticationService;
import app.domain.ports.EmployeePort;

/**
 * Caso de uso para login de usuarios.
 */
@Component
public class LoginUseCase {

    private static final Logger logger = LoggerFactory.getLogger(LoginUseCase.class);

    @Autowired
    private AuthenticationService authenticationService;
    
    @Autowired
    private EmployeePort employeePort;

    /**
     * Result object para devolver tanto token como datos del usuario.
     */
    public static class LoginResult {
        public TokenResponse tokenResponse;
        public Employee employee;

        public LoginResult(TokenResponse tokenResponse, Employee employee) {
            this.tokenResponse = tokenResponse;
            this.employee = employee;
        }
    }

    /**
     * Autentica a un usuario y devuelve tokens + información del empleado.
     */
    @app.infrastructure.audit.AuditableAction(action = "USER_LOGIN", description = "Inicio de sesión de usuario")
    public LoginResult login(AuthCredentials credentials) throws Exception {
        logger.info("Intento de login para documento: {}", credentials.getDocument());
        try {
            // Obtener datos del empleado
            Employee employee = employeePort.findByDocument(credentials.getDocument());
            if (employee == null) {
                throw new Exception("Usuario no encontrado");
            }
            
            // Autenticar y obtener tokens
            TokenResponse response = authenticationService.authenticate(credentials);
            
            logger.info("Login exitoso para documento: {} con rol: {}", 
                credentials.getDocument(), employee.getRole());
            
            return new LoginResult(response, employee);
        } catch (Exception e) {
            logger.warn("Login fallido para documento: {} - Razón: {}", 
                credentials.getDocument(), e.getMessage());
            throw e;
        }
    }
}