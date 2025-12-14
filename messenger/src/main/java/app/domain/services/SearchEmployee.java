package app.domain.services;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import app.domain.model.Employee;
import app.domain.ports.EmployeePort;

/**
 * Servicio de dominio para búsqueda y recuperación de empleados.
 * 
 * Proporciona múltiples métodos de búsqueda:
 * Por ID
 * Por número de documento
 * Por nombre de usuario
 * Listar todos los empleados
 */
@Service
public class SearchEmployee {

    @Autowired
    private EmployeePort employeePort;

    /**
     * Obtiene todos los empleados registrados.
     * 
     * @return Lista completa de empleados.
     */
    public List<Employee> findAll() {
        return employeePort.findAll();
    }

    /**
     * Busca un empleado por su número de documento.
     * 
     * @param document Número de documento.
     * @return Empleado encontrado.
     * @throws Exception Si el empleado no existe.
     */
    public Employee findByDocument(Long document) throws Exception {
        Employee employee = employeePort.findByDocument(document);
        if (employee == null) {
            throw new Exception("El empleado con documento " + document + " no existe.");
        }
        return employee;
    }

    /**
     * Busca un empleado por su ID.
     * 
     * @param id ID del empleado.
     * @return Empleado encontrado.
     * @throws RuntimeException Si el empleado no existe.
     */
    public Employee findById(Long id) {
        Employee employee = employeePort.findById(id);
        if (employee == null) {
            throw new RuntimeException("El empleado con ID " + id + " no existe.");
        }
        return employee;
    }

    /**
     * Busca un empleado por su nombre de usuario.
     * 
     * @param userName Nombre de usuario.
     * @return Empleado encontrado.
     * @throws RuntimeException Si el empleado no existe.
     */
    public Employee findByUserName(String userName) {
        Employee employee = employeePort.findByUserName(userName);
        if (employee == null) {
            throw new RuntimeException("El empleado con nombre de usuario " + userName + " no existe.");
        }
        return employee;
    }
}