package app.domain.ports;

import app.domain.model.auth.AuthCredentials;
import app.domain.model.auth.TokenResponse;

/**
 * Puerto (interfaz) para servicios de autenticación y gestión de tokens JWT.
 * 
 * Define las operaciones para autenticar usuarios, generar tokens de acceso,
 * validar tokens y extraer información de los mismos.
 */
public interface AuthenticationPort {
    TokenResponse authenticate(AuthCredentials credentials, String role);

    boolean validateToken(String token);

    String extractUsername(String token);

    String extractRole(String token);
}