package app.domain.services;

import java.util.regex.Pattern;

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

    private static final Pattern BCRYPT_PATTERN = Pattern.compile("\\A\\$2[ayb]\\$\\d\\d\\$[./A-Za-z0-9]{53}\\z");

    @Autowired
    private AuthenticationPort authenticationPort;
    @Autowired
    private EmployeePort employeePort;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public TokenResponse authenticate(AuthCredentials credentials) throws Exception {
        Employee employee = employeePort.findByUserName(credentials.getUserName());
        if (employee == null) {
            throw new BusinessException("Usuario no encontrado");
        }
        if (!passwordEncoder.matches(credentials.getPassword(), employee.getPassword())) {
            if (!isPasswordEncoded(employee.getPassword())
                    && credentials.getPassword().equals(employee.getPassword())) {
                String encoded = passwordEncoder.encode(credentials.getPassword());
                employee.setPassword(encoded);
                employeePort.save(employee);
            } else {
                throw new BusinessException("Contrasena incorrecta");
            }
        }
        return authenticationPort.authenticate(credentials, employee.getRole().name());
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
}