package app.domain.model.auth;

/**
 * Modelo de respuesta de autenticación (DTO).
 * 
 * Contiene:
 * - Access Token: JWT de corta duración para autenticar peticiones.
 * - Refresh Token: JWT de larga duración para renovar la sesión.
 * - Role: Rol del usuario autenticado.
 */
public class TokenResponse {
    private String token;
    private String refreshToken;
    private String role;

    /**
     * Obtiene el token de acceso JWT.
     * 
     * @return Token JWT como cadena de texto.
     */
    public String getToken() {
        return token;
    }

    /**
     * Establece el token de acceso JWT.
     * 
     * @param token Nuevo token JWT generada.
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * Obtiene el rol del usuario autenticado.
     * 
     * @return Rol del usuario (ej: "ADMIN", "MESSENGER").
     */
    public String getRole() {
        return role;
    }

    /**
     * Establece el rol del usuario autenticado.
     * 
     * @param role Rol a asignar.
     */
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * Obtiene el token de refresco.
     * 
     * @return Refresh token para renovar el session.
     */
    public String getRefreshToken() {
        return refreshToken;
    }

    /**
     * Establece el token de refresco.
     * 
     * @param refreshToken Nuevo refresh token.
     */
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}