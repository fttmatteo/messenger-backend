package app.application.usecase;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import app.domain.model.Employee;
import app.domain.model.enums.Role;
import app.domain.services.CreateEmployee;
import app.domain.services.DeleteEmployee;
import app.domain.services.SearchEmployee;
import app.domain.services.UpdateEmployee;


/**
 * Caso de uso para gestión de empleados.
 */
@Service
public class EmployeeUseCase {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeUseCase.class);

    private final CreateEmployee createEmployee;
    private final UpdateEmployee updateEmployee;
    private final SearchEmployee searchEmployee;
    private final DeleteEmployee deleteEmployee;

    public EmployeeUseCase(
            CreateEmployee createEmployee,
            UpdateEmployee updateEmployee,
            SearchEmployee searchEmployee,
            DeleteEmployee deleteEmployee) {
        this.createEmployee = createEmployee;
        this.updateEmployee = updateEmployee;
        this.searchEmployee = searchEmployee;
        this.deleteEmployee = deleteEmployee;
    }

    /**
     * Crea un nuevo empleado.
     */
    @CacheEvict(value = "employees", allEntries = true)

    public Employee create(Employee employee) throws Exception {
        Employee created = createEmployee.create(employee);
        logger.info("Empleado creado exitosamente - ID: {} | Rol: {}", 
                created.getIdEmployee(), created.getRole());
        return created;
    }

    /**
     * Actualiza los datos de un empleado existente.
     */
    @CacheEvict(value = "employees", allEntries = true)

    public Employee update(Long id, Employee employee) throws Exception {
        Employee updated = updateEmployee.update(id, employee);
        logger.info("Empleado ID {} actualizado exitosamente", id);
        return updated;
    }

    /**
     * Busca un empleado por su ID.
     */
    @Cacheable(value = "employees", key = "'id:' + #id")
    public Employee findById(Long id) {
        return searchEmployee.findById(id);
    }

    /**
     * Busca un empleado por su UUID público.
     */
    public Employee findByUuid(String uuid) {
        return searchEmployee.findByUuid(uuid);
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
     * Lista empleados filtrados por rol.
     */
    @Cacheable(value = "employees", key = "'role:' + #role.name()")
    public List<Employee> findByRole(Role role) {
        return searchEmployee.findByRole(role);
    }

    /**
     * Elimina un empleado del sistema.
     */
    @CacheEvict(value = "employees", allEntries = true)

    public void deleteById(Long id) throws Exception {
        deleteEmployee.deleteById(id);
        logger.info("Empleado ID {} eliminado exitosamente", id);
    }

    /**
     * Actualiza el perfil del usuario autenticado (solo campos permitidos).
     */
    @CacheEvict(value = "employees", allEntries = true)
    public Employee updateProfile(Long id, Employee incomingData) throws Exception {
        Employee existing = searchEmployee.findById(id);

        incomingData.setDocument(existing.getDocument());
        incomingData.setRole(existing.getRole());

        Employee updated = updateEmployee.update(id, incomingData);
        logger.info("Perfil de empleado ID {} actualizado", id);
        return updated;
    }
}