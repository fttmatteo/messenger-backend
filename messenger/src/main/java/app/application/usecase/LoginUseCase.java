package app.application.usecase;

import org.springframework.stereotype.Component;
import app.domain.model.auth.AuthCredentials;
import app.domain.model.auth.TokenResponse;
import app.domain.model.Employee;
import app.domain.exception.BusinessException;
import app.domain.services.AuthenticationService;
import app.domain.ports.EmployeePort;

/**
 * Caso de uso para login de usuarios.
 */
@Component
public class LoginUseCase {

    private final AuthenticationService authenticationService;
    private final EmployeePort employeePort;

    public LoginUseCase(AuthenticationService authenticationService, EmployeePort employeePort) {
        this.authenticationService = authenticationService;
        this.employeePort = employeePort;
    }

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

    public boolean validateToken(String token) {
        return authenticationService.validateToken(token);
    }

    public String extractUsername(String token) {
        return authenticationService.extractUsername(token);
    }

    public String extractRole(String token) {
        return authenticationService.extractRole(token);
    }

    public String generateWsToken(String username, String role) {
        return authenticationService.generateWsToken(username, role);
    }

    /**
     * Autentica a un usuario y devuelve tokens + información del empleado.
     */
    public LoginResult login(AuthCredentials credentials) {
        try {
            Employee employee = employeePort.findByDocument(credentials.getDocument());
            if (employee == null) {
                throw new BusinessException("Usuario no encontrado");
            }

            TokenResponse response = authenticationService.authenticate(credentials);
            return new LoginResult(response, employee);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("Error durante autenticación: " + e.getMessage());
        }
    }
}