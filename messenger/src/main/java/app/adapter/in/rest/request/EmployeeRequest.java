package app.adapter.in.rest.request;

/**
 * DTO (Data Transfer Object) para las peticiones de creación y actualización de
 * empleados.
 * 
 * Este objeto encapsula toda la información necesaria para registrar o
 * modificar
 * un empleado en el sistema, incluyendo datos personales, credenciales de
 * autenticación
 * y asignación de roles.
 * 
 * Campos incluidos:
 * - document: Documento de identidad único (Cédula, DNI, etc.)
 * - fullName: Nombre completo del empleado
 * - phone: Número de teléfono de contacto
 * - userName: Nombre de usuario para autenticación
 * - password: Contraseña (se almacenará encriptada)
 * - role: Rol del empleado (ADMIN, MESSENGER, etc.)
 * 
 * @see app.adapter.in.rest.controllers.EmployeeController
 * @see app.domain.model.Employee
 * @see app.domain.model.enums.Role
 */
public class EmployeeRequest {
    private String document;
    private String fullName;
    private String phone;
    private String userName;
    private String password;
    private String role;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getDocument() {
        return document;
    }

    public void setDocument(String document) {
        this.document = document;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}