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

    /**
     * Obtiene el nombre de usuario.
     * 
     * @return Nombre de usuario.
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Establece el nombre de usuario.
     * 
     * @param userName Nombre de usuario.
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Obtiene la contraseña.
     * 
     * @return Contraseña del usuario.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Establece la contraseña.
     * 
     * @param password Contraseña del usuario.
     */
    public void setPassword(String password) {
        this.password = password;
    }
}