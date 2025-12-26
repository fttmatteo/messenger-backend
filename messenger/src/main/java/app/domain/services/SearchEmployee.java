package app.domain.services;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import app.domain.model.Employee;
import app.domain.ports.EmployeePort;

/**
 * Servicio para búsqueda de empleados.
 */
@Service
public class SearchEmployee {

    @Autowired
    private EmployeePort employeePort;

    /**
     * Recupera todos los empleados registrados.
     */
    public List<Employee> findAll() {
        return employeePort.findAll();
    }

    /**
     * Busca un empleado por su ID interno.
     */
    public Employee findById(Long id) {
        Employee employee = employeePort.findById(id);
        if (employee == null) {
            throw new RuntimeException("El empleado con ID " + id + " no existe.");
        }
        return employee;
    }

    /**
     * Busca un empleado por su número de documento.
     */
    public Employee findByDocument(Long document) {
        Employee employee = employeePort.findByDocument(document);
        if (employee == null) {
            throw new RuntimeException("El empleado con documento " + document + " no existe.");
        }
        return employee;
    }
}