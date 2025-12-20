package app.domain.services;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import app.domain.model.Employee;
import app.domain.ports.EmployeePort;

/**
 * Servicio de dominio para búsqueda y recuperación de empleados.
 */
@Service
public class SearchEmployee {

    @Autowired
    private EmployeePort employeePort;

    public List<Employee> findAll() {
        return employeePort.findAll();
    }

    public Employee findById(Long id) {
        Employee employee = employeePort.findById(id);
        if (employee == null) {
            throw new RuntimeException("El empleado con ID " + id + " no existe.");
        }
        return employee;
    }

    public Employee findByUserName(String userName) {
        Employee employee = employeePort.findByUserName(userName);
        if (employee == null) {
            throw new RuntimeException("El empleado con nombre de usuario " + userName + " no existe.");
        }
        return employee;
    }
}