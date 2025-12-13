package app.domain.ports;

import app.domain.model.Employee;
import java.util.List;

/**
 * Puerto (interfaz) para operaciones de persistencia de empleados.
 * 
 * Define las operaciones CRUD y consultas necesarias para gestionar empleados
 * y mensajeros del sistema.
 */
public interface EmployeePort {
    void save(Employee employee);

    void deleteById(Long idEmployee);

    void deleteByDocument(Long document);

    Employee findById(Long idEmployee);

    Employee findByDocument(Long document);

    Employee findByUserName(String userName);

    List<Employee> findAll();
}