// Archivo: app/domain/ports/AuthenticationPort.java
package app.domain.ports;

import app.domain.model.auth.AuthCredentials;
import app.domain.model.auth.TokenResponse;

public interface AuthenticationPort {
    // Genera el token
    TokenResponse authenticate(AuthCredentials credentials, String role);
    // Valida si el token es correcto
    boolean validateToken(String token);
    // Extrae info del token
    String extractUsername(String token);
    String extractRole(String token);
}