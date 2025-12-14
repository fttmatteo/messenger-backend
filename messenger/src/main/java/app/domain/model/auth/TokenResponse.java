package app.domain.model.auth;

/**
 * Modelo de respuesta que contiene el token JWT generado tras autenticación
 * exitosa.
 * 
 * Incluye el token de acceso y el rol del usuario autenticado.
 */
public class TokenResponse {
    private String token;
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
}