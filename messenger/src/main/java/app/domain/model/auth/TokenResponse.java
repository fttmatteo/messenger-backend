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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}