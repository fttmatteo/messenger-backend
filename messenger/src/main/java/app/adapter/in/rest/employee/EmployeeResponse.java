package app.adapter.in.rest.employee;

import app.domain.model.enums.Role;

/**
 * DTO de respuesta con datos de empleado.
 */
public class EmployeeResponse {
    private Long idEmployee;
    private String uuid;
    private Long document;
    private String fullName;
    private String phone;
    private Role role;

    public EmployeeResponse() {
    }

    public EmployeeResponse(Long idEmployee, String uuid, Long document, String fullName, String phone, Role role) {
        this.idEmployee = idEmployee;
        this.uuid = uuid;
        this.document = document;
        this.fullName = fullName;
        this.phone = phone;
        this.role = role;
    }

    public Long getIdEmployee() {
        return idEmployee;
    }

    public void setIdEmployee(Long idEmployee) {
        this.idEmployee = idEmployee;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
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

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
