package app.domain.model.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO para la solicitud de refresco de token.
 * 
 * Encapsula el refresh token necesario para solicitar un nuevo par
 * de tokens (access + refresh) cuando el access token ha expirado.
 */
public class RefreshTokenRequest {

    @NotBlank(message = "El refresh token es obligatorio")
    private String refreshToken;

    /**
     * Obtiene el refresh token.
     * 
     * @return Token de refresco.
     */
    public String getRefreshToken() {
        return refreshToken;
    }

    /**
     * Establece el refresh token.
     * 
     * @param refreshToken Token de refresco a establecer.
     */
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
