package app.infrastructure.persistence.entities;

import app.domain.model.enums.Role;
import jakarta.persistence.*;

import jakarta.persistence.*;

/**
 * Entidad JPA que representa la tabla 'employees'.
 * Almacena la información de los empleados, sus credenciales y roles.
 */
@Entity
@Table(name = "employees")
public class EmployeeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_employee")
    private Long idEmployee;

    @Column(unique = true, nullable = false)
    private Long document;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(length = 20)
    private String phone;

    @Column(name = "user_name", unique = true, nullable = false)
    private String userName;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
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