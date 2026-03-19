package app.adapter.in.rest.mapper;

import app.adapter.in.rest.response.EmployeeResponse;
import app.domain.model.Employee;
import org.springframework.stereotype.Component;

/**
 * Mapper de Employee a EmployeeResponse para API REST.
 */
@Component
public class EmployeeResponseMapper {

    public EmployeeResponse toResponse(Employee employee) {
        if (employee == null) {
            return null;
        }

        return new EmployeeResponse(
                employee.getIdEmployee(),
                employee.getUuid(),
                employee.getDocument(),
                employee.getFullName(),
                employee.getPhone(),
                employee.getRole());
    }
}
