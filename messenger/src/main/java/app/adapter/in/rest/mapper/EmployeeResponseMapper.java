package app.adapter.in.rest.mapper;

import app.adapter.in.rest.response.EmployeeResponse;
import app.domain.model.Employee;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entidades Employee a DTOs de respuesta.
 * 
 * <p>
 * Transforma objetos de dominio Employee a EmployeeResponse.
 * </p>
 */
@Component
public class EmployeeResponseMapper {

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
