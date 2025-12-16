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
 * Caso de uso de aplicación para gestionar empleados.
 * 
 * Orquesta las operaciones CRUD de empleados delegando en los servicios de
 * dominio
 * correspondientes. Sirve como punto de entrada desde la capa de adaptadores.
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
     * Crea un nuevo empleado en el sistema.
     * 
     * @param employee La entidad del empleado a crear.
     * @throws Exception Si ocurre un error durante la creación.
     */
    public void create(Employee employee) throws Exception {
        logger.debug("UseCase: creando empleado {}", employee.getUserName());
        createEmployee.create(employee);
    }

    /**
     * Actualiza la información de un empleado existente.
     * 
     * @param id       El ID del empleado a actualizar.
     * @param employee Los nuevos datos del empleado.
     * @throws Exception Si el empleado no existe o hay un error en la
     *                   actualización.
     */
    public void update(Long id, Employee employee) throws Exception {
        logger.debug("UseCase: actualizando empleado ID {}", id);
        updateEmployee.update(id, employee);
    }

    /**
     * Busca un empleado por su ID único.
     * 
     * @param id El ID del empleado.
     * @return El empleado encontrado.
     */
    public Employee findById(Long id) {
        return searchEmployee.findById(id);
    }

    /**
     * Busca un empleado por su número de documento.
     * 
     * @param document El número de documento del empleado.
     * @return El empleado encontrado.
     * @throws Exception Si no se encuentra el empleado.
     */
    public Employee findByDocument(Long document) throws Exception {
        return searchEmployee.findByDocument(document);
    }

    /**
     * Busca un empleado por su nombre de usuario.
     * 
     * @param userName El nombre de usuario del empleado.
     * @return El empleado encontrado.
     * @throws Exception Si no se encuentra el empleado.
     */
    public Employee findByUserName(String userName) throws Exception {
        return searchEmployee.findByUserName(userName);
    }

    /**
     * Obtiene una lista de todos los empleados registrados.
     * 
     * @return Lista completa de empleados.
     */
    public List<Employee> findAll() {
        return searchEmployee.findAll();
    }

    /**
     * Elimina un empleado por su ID.
     * 
     * @param id El ID del empleado a eliminar.
     * @throws Exception Si el empleado no existe o no se puede eliminar.
     */
    public void deleteById(Long id) throws Exception {
        logger.debug("UseCase: eliminando empleado ID {}", id);
        deleteEmployee.deleteById(id);
    }

    /**
     * Elimina un empleado por su número de documento.
     * 
     * @param document El documento del empleado a eliminar.
     * @throws Exception Si el empleado no existe o no se puede eliminar.
     */
    public void deleteByDocument(Long document) throws Exception {
        deleteEmployee.deleteByDocument(document);
    }
}