package app.domain.ports;

import app.domain.model.Employee;
import app.domain.model.enums.Role;
import java.util.List;

/**
 * Puerto de salida para persistencia de empleados.
 */
public interface EmployeePort {

    Employee save(Employee employee);

    void deleteById(Long idEmployee);

    Employee findById(Long idEmployee);

    Employee findByDocument(Long document);

    List<Employee> findAll();

    Employee findByUuid(String uuid);

    List<Employee> findByRole(Role role);
}