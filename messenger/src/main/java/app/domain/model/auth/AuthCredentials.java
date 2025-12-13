package app.domain.model.auth;

/**
 * Modelo que encapsula las credenciales de autenticación del usuario.
 * 
 * Contiene los datos necesarios para validar la identidad de un usuario
 * durante el proceso de login.
 */
public class AuthCredentials {
    private String userName;
    private String password;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}