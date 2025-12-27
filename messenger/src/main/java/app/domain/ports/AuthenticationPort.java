package app.domain.ports;

import app.domain.model.auth.AuthCredentials;
import app.domain.model.auth.TokenResponse;

/**
 * Puerto de salida para operaciones de autenticación JWT.
 */
public interface AuthenticationPort {

    /**
     * Autentica un usuario y genera un token de acceso.
     */
    TokenResponse authenticate(AuthCredentials credentials, String role, Long userId);

    /**
     * Valida la firma y vigencia de un token JWT.
     */
    boolean validateToken(String token);

    /**
     * Extrae el nombre de usuario (subject) de un token.
     */
    String extractUsername(String token);

    /**
     * Extrae el rol contenido en los claims del token.
     */
    String extractRole(String token);

    /**
     * Genera un token de refresco (refresh token) para renovaciones.
     */
    String generateRefreshToken(AuthCredentials credentials);

    /**
     * Valida la integridad de un refresh token.
     */
    boolean validateRefreshToken(String token);
}