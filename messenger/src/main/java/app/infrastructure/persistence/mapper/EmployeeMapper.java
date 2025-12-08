package app.infrastructure.persistence.mapper;

import app.domain.model.Employee;
import app.infrastructure.persistence.entities.EmployeeEntity;
import org.springframework.stereotype.Component;

@Component
public class EmployeeMapper {

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