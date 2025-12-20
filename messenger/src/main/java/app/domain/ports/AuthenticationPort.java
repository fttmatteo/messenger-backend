package app.domain.ports;

import app.domain.model.auth.AuthCredentials;
import app.domain.model.auth.TokenResponse;

public interface AuthenticationPort {

    TokenResponse authenticate(AuthCredentials credentials, String role, Long userId);

    boolean validateToken(String token);

    String extractUsername(String token);

    String extractRole(String token);

    String generateRefreshToken(AuthCredentials credentials);

    boolean validateRefreshToken(String token);
}