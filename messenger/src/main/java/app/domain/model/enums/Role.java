package app.domain.model.enums;

/**
 * Enumeración de roles de usuario en el sistema.
 * 
 * Define los niveles de acceso y permisos:
 * ADMIN: Administrador con acceso completo al sistema
 * MESSENGER: Mensajero que realiza entregas
 */
public enum Role {
    /**
     * Administrador del sistema.
     * Tiene acceso completo a todas las funcionalidades, configuración y reportes.
     */
    ADMIN,

    /**
     * Mensajero de campo.
     * Su acceso está limitado a la gestión de sus entregas asignadas.
     */
    MESSENGER
}
