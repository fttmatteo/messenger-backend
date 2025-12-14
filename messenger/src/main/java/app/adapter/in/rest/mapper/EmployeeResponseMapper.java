package app.adapter.in.rest.mapper;

import app.adapter.in.rest.response.EmployeeResponse;
import app.domain.model.Employee;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entidades Employee a DTOs de respuesta.
 * Transforma objetos de dominio Employee a EmployeeResponse.
 */
@Component
public class EmployeeResponseMapper {

    /**
     * Convierte una entidad Employee a EmployeeResponse.
     *
     * @param employee Entidad Employee de origen.
     * @return DTO EmployeeResponse poblado o null si la entrada es nula.
     */
    public EmployeeResponse toResponse(Employee employee) {
        if (employee == null) {
            return null;
        }

        return new EmployeeResponse(
                employee.getIdEmployee(),
                employee.getDocument(),
                employee.getFullName(),
                employee.getPhone(),
                employee.getUserName(),
                employee.getRole());
    }
}
