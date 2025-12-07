package app.domain.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import app.application.exceptions.BusinessException;
import app.domain.model.Employee;
import app.domain.model.auth.AuthCredentials;
import app.domain.model.auth.TokenResponse;
import app.domain.ports.AuthenticationPort;
import app.domain.ports.EmployeePort;

@Service
public class AuthenticationService {

    @Autowired
    private AuthenticationPort authenticationPort;
    @Autowired
    private EmployeePort employeePort;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public TokenResponse authenticate(AuthCredentials credentials) throws Exception {
        Employee query = new Employee();
        query.setUserName(credentials.getUserName());
        Employee employee = employeePort.findByUserName(query);

        if (employee == null) {
            throw new BusinessException("Usuario no encontrado");
        }

        if (!isPasswordValid(credentials.getPassword(), employee)) {
            throw new BusinessException("Contraseña incorrecta");
        }

        return authenticationPort.authenticate(credentials, String.valueOf(employee.getRole()));
    }

    private boolean isPasswordValid(String rawPassword, Employee employee) throws Exception {
        String storedPassword = employee.getPassword();

        if (passwordEncoder.matches(rawPassword, storedPassword)) {
            return true;
        }

        if (!isBcryptPattern(storedPassword) && rawPassword.equals(storedPassword)) {
            String encoded = passwordEncoder.encode(rawPassword);
            employee.setPassword(encoded);
            employeePort.save(employee);
            return true;
        }

        return false;
    }

    private boolean isBcryptPattern(String password) {
        return password != null && password.startsWith("$2a$");
    }
}