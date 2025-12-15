package app.adapter.in.rest.response;

import app.domain.model.enums.Role;

/**
 * DTO (Data Transfer Object) de respuesta con la información de un empleado.
 * 
 * Este objeto encapsula los datos de un empleado registrado en el sistema,
 * excluyendo información sensible como la contraseña por razones de seguridad.
 * 
 * Campos incluidos:
 * - idEmployee: Identificador único del empleado
 * - document: Documento de identidad
 * - fullName: Nombre completo
 * - phone: Número de teléfono
 * - userName: Nombre de usuario para autenticación
 * - role: Rol asignado (ADMIN, MESSENGER, etc.)
 * 
 * Nota: La contraseña nunca se incluye en las respuestas por seguridad.
 * 
 * @see app.adapter.in.rest.controllers.EmployeeController
 * @see app.domain.model.Employee
 * @see app.domain.model.enums.Role
 */
public class EmployeeResponse {
    private Long idEmployee;
    private Long document;
    private String fullName;
    private String phone;
    private String userName;
    private Role role;

    public EmployeeResponse() {
    }

    public EmployeeResponse(Long idEmployee, Long document, String fullName, String phone, String userName, Role role) {
        this.idEmployee = idEmployee;
        this.document = document;
        this.fullName = fullName;
        this.phone = phone;
        this.userName = userName;
        this.role = role;
    }

    public Long getIdEmployee() {
        return idEmployee;
    }

    public void setIdEmployee(Long idEmployee) {
        this.idEmployee = idEmployee;
    }

    public Long getDocument() {
        return document;
    }

    public void setDocument(Long document) {
        this.document = document;
    }

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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
