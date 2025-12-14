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
    /**
     * Guarda o actualiza un empleado en la base de datos.
     * 
     * @param employee Empleado a guardar.
     */
    void save(Employee employee);

    /**
     * Elimina un empleado por su ID.
     * 
     * @param idEmployee ID del empleado a eliminar.
     */
    void deleteById(Long idEmployee);

    /**
     * Elimina un empleado por su número de documento.
     * 
     * @param document Número de documento del empleado.
     */
    void deleteByDocument(Long document);

    /**
     * Busca un empleado por su ID.
     * 
     * @param idEmployee ID del empleado.
     * @return Empleado encontrado o null si no existe.
     */
    Employee findById(Long idEmployee);

    /**
     * Busca un empleado por su número de documento.
     * 
     * @param document Número de documento.
     * @return Empleado encontrado o null si no existe.
     */
    Employee findByDocument(Long document);

    /**
     * Busca un empleado por su nombre de usuario.
     * 
     * @param userName Nombre de usuario.
     * @return Empleado encontrado o null si no existe.
     */
    Employee findByUserName(String userName);

    /**
     * Obtiene todos los empleados registrados.
     * 
     * @return Lista de todos los empleados.
     */
    List<Employee> findAll();
}