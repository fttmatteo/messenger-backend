package app.domain.services;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import app.domain.model.Employee;
import app.domain.model.enums.Role;
import app.domain.ports.EmployeePort;
import app.domain.util.LogSanitizer;

/**
 * Servicio para búsqueda de empleados.
 */
@Service
public class SearchEmployee {

    private static final Logger logger = LoggerFactory.getLogger(SearchEmployee.class);

    @Autowired
    private EmployeePort employeePort;

    /**
     * Recupera todos los empleados registrados.
     */
    public List<Employee> findAll() {
        return employeePort.findAll();
    }

    /**
     * Busca empleados filtrados por rol.
     */
    public List<Employee> findByRole(Role role) {
        return employeePort.findByRole(role);
    }

    /**
     * Busca un empleado por su ID interno.
     */
    public Employee findById(Long id) {
        Employee employee = employeePort.findById(id);
        if (employee == null) {
            logger.warn("Empleado no encontrado: ID {}", id);
            throw new RuntimeException("El empleado no existe.");
        }
        return employee;
    }

    /**
     * Busca un empleado por su número de documento.
     */
    public Employee findByDocument(Long document) {
        Employee employee = employeePort.findByDocument(document);
        if (employee == null) {
            logger.warn("Empleado no encontrado: documento {}", LogSanitizer.maskDocument(document));
            throw new RuntimeException("El empleado no existe.");
        }
        return employee;
    }

    /**
     * Busca un empleado por su UUID público.
     */
    public Employee findByUuid(String uuid) {
        Employee employee = employeePort.findByUuid(uuid);
        if (employee == null) {
            logger.warn("Empleado no encontrado: UUID {}", uuid);
            throw new RuntimeException("El empleado no existe.");
        }
        return employee;
    }
}
