package app.application.usecase;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import app.domain.model.Employee;
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

    @Autowired
    private CreateEmployee createEmployee;
    @Autowired
    private UpdateEmployee updateEmployee;
    @Autowired
    private SearchEmployee searchEmployee;
    @Autowired
    private DeleteEmployee deleteEmployee;

    public Employee create(Employee employee) throws Exception {
        logger.info("Creando empleado con documento: {}, rol: {}", employee.getDocument(), employee.getRole());
        Employee created = createEmployee.create(employee);
        logger.info("Empleado creado con ID: {}", created.getIdEmployee());
        return created;
    }

    public Employee update(Long id, Employee employee) throws Exception {
        logger.info("Actualizando empleado ID: {}", id);
        Employee updated = updateEmployee.update(id, employee);
        logger.info("Empleado ID: {} actualizado", id);
        return updated;
    }

    public Employee findById(Long id) {
        return searchEmployee.findById(id);
    }

    public Employee findByDocument(Long document) throws Exception {
        return searchEmployee.findByDocument(document);
    }

    public List<Employee> findAll() {
        return searchEmployee.findAll();
    }

    public void deleteById(Long id) throws Exception {
        logger.warn("Eliminando empleado ID: {}", id);
        deleteEmployee.deleteById(id);
        logger.info("Empleado ID: {} eliminado", id);
    }
}