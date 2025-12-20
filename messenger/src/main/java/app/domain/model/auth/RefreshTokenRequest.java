package app.domain.model.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * Solicitud para renovar el token de acceso usando un refresh token.
 */
public class RefreshTokenRequest {

    @NotBlank(message = "El refresh token es obligatorio")
    private String refreshToken;

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
