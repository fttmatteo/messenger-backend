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
    /**
     * Autentica un usuario y genera un token JWT.
     * 
     * @param credentials Credenciales de acceso (usuario y contraseña).
     * @param role        Rol esperado del usuario.
     * @return TokenResponse con el token JWT y rol del usuario.
     */
    TokenResponse authenticate(AuthCredentials credentials, String role);

    /**
     * Valida si un token JWT es válido y no ha expirado.
     * 
     * @param token Token JWT a validar.
     * @return true si el token es válido, false en caso contrario.
     */
    boolean validateToken(String token);

    /**
     * Extrae el nombre de usuario del token JWT.
     * 
     * @param token Token JWT.
     * @return Nombre de usuario contenido en el token.
     */
    String extractUsername(String token);

    /**
     * Extrae el rol del usuario del token JWT.
     * 
     * @param token Token JWT.
     * @return Rol del usuario (ADMIN, MESSENGER).
     */
    String extractRole(String token);
}