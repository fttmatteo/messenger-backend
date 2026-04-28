package app.domain.model.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Credenciales de autenticación con documento, contraseña y token de Turnstile.
 */
public class AuthCredentials {

    @NotNull(message = "El documento es requerido")
    private Long document;

    @NotBlank(message = "La contraseña es requerida")
    @Size(min = 4, max = 128, message = "La contraseña debe tener entre 4 y 128 caracteres")
    private String password;

    @NotBlank(message = "El token de verificación es requerido")
    private String turnstileToken;

    public Long getDocument() {
        return document;
    }

    public void setDocument(Long document) {
        this.document = document;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTurnstileToken() {
        return turnstileToken;
    }

    public void setTurnstileToken(String turnstileToken) {
        this.turnstileToken = turnstileToken;
    }
}
