package app.domain.model.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * Credenciales de autenticación con documento, contraseña y token de Turnstile.
 */
public class AuthCredentials {
    private Long document;
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
