package app.infrastructure.persistence.entities;

import app.domain.model.enums.Role;
import jakarta.persistence.*;

/**
 * Entidad JPA que representa la tabla 'employees'.
 * 
 * Almacena la información de los empleados (administradores y mensajeros),
 * sus credenciales de acceso y roles en el sistema.
 * 
 * Relaciones:
 * - Un empleado puede ser asignado a múltiples ServiceDelivery como mensajero
 * - Un empleado puede realizar múltiples cambios de estado (StatusHistory)
 */
@Entity
@Table(name = "employees")
public class EmployeeEntity {

    /** Identificador único del empleado (clave primaria). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_employee")
    private Long idEmployee;

    /** Número de documento de identidad (único en el sistema). */
    @Column(unique = true, nullable = false)
    private Long document;

    /** Nombre completo del empleado. */
    @Column(name = "full_name", nullable = false)
    private String fullName;

    /** Número de teléfono de contacto. */
    @Column(length = 20)
    private String phone;

    /** Nombre de usuario para autenticación (único en el sistema). */
    @Column(name = "user_name", unique = true, nullable = false)
    private String userName;

    /** Contraseña encriptada con BCrypt. */
    @Column(nullable = false)
    private String password;

    /** Rol del empleado en el sistema (ADMIN o MESSENGER). */
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