package app.adapter.in.rest.request;

/**
 * DTO para crear o actualizar un empleado.
 * 
 * <p>
 * Contiene datos personales, credenciales de acceso y rol del empleado.
 * </p>
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