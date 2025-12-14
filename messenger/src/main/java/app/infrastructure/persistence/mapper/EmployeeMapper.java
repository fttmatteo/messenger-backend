package app.infrastructure.persistence.mapper;

import app.domain.model.Employee;
import app.infrastructure.persistence.entities.EmployeeEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper de persistencia para convertir entre Employee y EmployeeEntity.
 * Facilita la transformación de datos entre la capa de dominio y la base de
 * datos.
 */
@Component
public class EmployeeMapper {

    /**
     * Convierte un modelo de dominio Employee a su entidad JPA correspondiente.
     * 
     * Mapea todos los campos del empleado incluyendo documento, nombre completo,
     * teléfono, credenciales de acceso y rol para persistencia en base de datos.
     * 
     * @param employee El modelo de dominio a convertir (puede ser null)
     * @return La entidad JPA correspondiente, o null si el parámetro es null
     */
    public EmployeeEntity toEntity(Employee employee) {
        if (employee == null)
            return null;
        EmployeeEntity entity = new EmployeeEntity();
        entity.setIdEmployee(employee.getIdEmployee());
        entity.setDocument(employee.getDocument());
        entity.setFullName(employee.getFullName());
        entity.setPhone(employee.getPhone());
        entity.setUserName(employee.getUserName());
        entity.setPassword(employee.getPassword());
        entity.setRole(employee.getRole());
        return entity;
    }

    /**
     * Convierte una entidad JPA EmployeeEntity a modelo de dominio.
     * 
     * Reconstruye el objeto de dominio completo desde la base de datos,
     * incluyendo todos los datos de identificación, contacto y autenticación.
     * 
     * @param entity La entidad JPA a convertir (puede ser null)
     * @return El modelo de dominio correspondiente, o null si la entidad es null
     */
    public Employee toDomain(EmployeeEntity entity) {
        if (entity == null)
            return null;
        Employee employee = new Employee();
        employee.setIdEmployee(entity.getIdEmployee());
        employee.setDocument(entity.getDocument());
        employee.setFullName(entity.getFullName());
        employee.setPhone(entity.getPhone());
        employee.setUserName(entity.getUserName());
        employee.setPassword(entity.getPassword());
        employee.setRole(entity.getRole());
        return employee;
    }

}