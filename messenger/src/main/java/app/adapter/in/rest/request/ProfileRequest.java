package app.adapter.in.rest.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO para la actualización del perfil propio del usuario.
 * Solo contiene campos que el usuario tiene permitido modificar sobre sí mismo.
 */
public class ProfileRequest {

    @NotBlank(message = "El nombre es requerido")
    @Size(min = 3, message = "El nombre debe tener al menos 3 caracteres")
    private String fullName;

    @NotBlank(message = "El teléfono es requerido")
    @Pattern(regexp = "^\\d{10}$", message = "El teléfono debe tener 10 dígitos")
    private String phone;

    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
