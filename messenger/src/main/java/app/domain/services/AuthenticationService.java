package app.domain.services;

import java.util.regex.Pattern;
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

@Service
public class AuthenticationService {

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
        TokenResponse response = authenticationPort.authenticate(credentials, String.valueOf(employee.getRole()),
                employee.getIdEmployee());
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

    public TokenResponse refreshToken(String refreshToken) throws Exception {
        if (tokenBlacklistService.isBlacklisted(refreshToken)) {
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

        TokenResponse response = authenticationPort.authenticate(credentials, String.valueOf(employee.getRole()),
                employee.getIdEmployee());

        String newRefreshToken = authenticationPort.generateRefreshToken(credentials);
        response.setRefreshToken(newRefreshToken);

        long refreshTokenTtlSeconds = (jwtExpiration * 24) / 1000;
        tokenBlacklistService.addToBlacklist(refreshToken, refreshTokenTtlSeconds);

        return response;
    }
}