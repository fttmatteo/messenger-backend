package app.adapter.in.rest.response;

import app.domain.model.enums.Role;

public class EmployeeResponse {
    private Long idEmployee;
    private Long document;
    private String fullName;
    private String phone;
    private Role role;

    public EmployeeResponse() {
    }

    public EmployeeResponse(Long idEmployee, Long document, String fullName, String phone, Role role) {
        this.idEmployee = idEmployee;
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
