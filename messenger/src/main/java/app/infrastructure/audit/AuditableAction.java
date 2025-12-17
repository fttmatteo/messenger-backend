package app.infrastructure.audit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotación para marcar métodos que requieren auditoría administrativa.
 * 
 * Los métodos anotados con @AuditableAction serán interceptados por
 * AuditAspect para generar logs de auditoría con información sobre:
 * - Usuario que ejecutó la acción
 * - Tipo de acción realizada
 * - Parámetros de la operación
 * - Resultado (éxito/fallo)
 * - Timestamp
 * 
 * Uso:
 * 
 * <pre>
 * {@code @AuditableAction(action = "CREATE_EMPLOYEE")}
 * public Employee createEmployee(EmployeeRequest request) { ... }
 * </pre>
 * 
 * @see AuditAspect
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditableAction {

    /**
     * Nombre descriptivo de la acción para el log de auditoría.
     * Ejemplos: "CREATE_EMPLOYEE", "DELETE_EMPLOYEE", "UPDATE_ROLE"
     */
    String action();

    /**
     * Descripción opcional de la acción.
     */
    String description() default "";
}
