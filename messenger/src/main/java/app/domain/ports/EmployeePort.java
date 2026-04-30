package app.domain.ports;

import app.domain.model.Employee;
import app.domain.model.enums.Role;
import java.util.List;

/**
 * Puerto de salida para persistencia de empleados.
 */
public interface EmployeePort {

    /**
     * Guarda o actualiza un empleado.
     */
    Employee save(Employee employee);

    /**
     * Elimina un empleado del sistema.
     */
    void deleteById(Long idEmployee);

    /**
     * Busca un empleado por su ID.
     */
    Employee findById(Long idEmployee);

    /**
     * Busca un empleado por su número de documento.
     */
    Employee findByDocument(Long document);

    /**
     * Recupera todos los empleados registrados.
     */
    List<Employee> findAll();

    /**
     * Busca un empleado por su UUID público.
     */
    Employee findByUuid(String uuid);

    /**
     * Busca empleados filtrados por rol.
     */
    List<Employee> findByRole(Role role);
}