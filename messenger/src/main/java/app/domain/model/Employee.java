package app.domain.model;

import app.domain.model.enums.Role;

/**
 * Modelo de dominio que representa un empleado del sistema de mensajería.
 * 
 * Un empleado puede ser un mensajero encargado de realizar entregas o
 * un administrador del sistema. La clase almacena información personal,
 * credenciales de acceso y rol asignado.
 * 
 * Roles disponibles:
 * MESSENGER: Mensajero que realiza entregas
 * ADMIN: Administrador del sistema
 */
public class Employee {
    private Long idEmployee;
    private Long document;
    private String fullName;
    private String phone;
    private String userName;
    private String password;
    private Role role;

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}