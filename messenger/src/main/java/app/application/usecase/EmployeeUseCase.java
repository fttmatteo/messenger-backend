package app.application.usecase;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import app.domain.model.Employee;
import app.domain.services.CreateEmployee;
import app.domain.services.DeleteEmployee;
import app.domain.services.SearchEmployee;
import app.domain.services.UpdateEmployee;
import app.infrastructure.audit.AuditableAction;

/**
 * Caso de uso para gestión de empleados.
 */
@Service
public class EmployeeUseCase {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeUseCase.class);

    @Autowired
    private CreateEmployee createEmployee;
    @Autowired
    private UpdateEmployee updateEmployee;
    @Autowired
    private SearchEmployee searchEmployee;
    @Autowired
    private DeleteEmployee deleteEmployee;

    /**
     * Crea un nuevo empleado.
     */
    @CacheEvict(value = "employees", allEntries = true)
    @AuditableAction(action = "CREATE_EMPLOYEE", description = "Crear nuevo empleado")
    public Employee create(Employee employee) throws Exception {
        Employee created = createEmployee.create(employee);
        return created;
    }

    /**
     * Actualiza los datos de un empleado existente.
     */
    @CacheEvict(value = "employees", allEntries = true)
    @AuditableAction(action = "UPDATE_EMPLOYEE", description = "Actualizar empleado")
    public Employee update(Long id, Employee employee) throws Exception {
        Employee updated = updateEmployee.update(id, employee);
        return updated;
    }

    /**
     * Busca un empleado por su ID.
     */
    @Cacheable(value = "employees", key = "'id:' + #id")
    public Employee findById(Long id) {
        Employee employee = searchEmployee.findById(id);
        if (employee == null) {
            throw new RuntimeException("Empleado no encontrado con ID: " + id);
        }
        return employee;
    }

    /**
     * Busca un empleado por su número de documento.
     */
    public Employee findByDocument(Long document) throws Exception {
        return searchEmployee.findByDocument(document);
    }

    /**
     * Lista todos los empleados registrados.
     */
    @Cacheable(value = "employees", key = "'all'")
    public List<Employee> findAll() {
        return searchEmployee.findAll();
    }

    /**
     * Elimina un empleado del sistema.
     */
    @CacheEvict(value = "employees", allEntries = true)
    @AuditableAction(action = "DELETE_EMPLOYEE", description = "Eliminar empleado")
    public void deleteById(Long id) throws Exception {
        deleteEmployee.deleteById(id);
        logger.warn("Eliminando empleado ID: {}", id);
    }
}