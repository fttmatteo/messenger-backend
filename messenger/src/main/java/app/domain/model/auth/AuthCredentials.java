package app.domain.model.auth;

/**
 * Credenciales de autenticación con documento y contraseña.
 */
public class AuthCredentials {
    private Long document;
    private String password;

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
}