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

    private static final Pattern BCRYPT_PATTERN = Pattern.compile("^\\$2[ayb]\\$\\d{2}\\$[./A-Za-z0-9]{53}$");

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

        String storedPassword = employee.getPassword();
        String inputPassword = credentials.getPassword();

        if (!passwordEncoder.matches(inputPassword, storedPassword)) {
            if (!isPasswordEncoded(storedPassword) && inputPassword.equals(storedPassword)) {
                String encoded = passwordEncoder.encode(inputPassword);
                employee.setPassword(encoded);
                employeePort.save(employee);
            } else {
                throw new BusinessException("Contraseña incorrecta");
            }
        }
        return authenticationPort.authenticate(credentials, String.valueOf(employee.getRole()));
    }

    private boolean isPasswordEncoded(String password) {
        if (password == null)
            return false;
        String cleanPassword = password.startsWith("{bcrypt}") ? password.substring(8) : password;
        return BCRYPT_PATTERN.matcher(cleanPassword).matches();
    }
}