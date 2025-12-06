package app.infrastructure.persistence.mapper;

import app.domain.model.Employee;
import app.domain.model.enums.Role;
import app.infrastructure.persistence.entities.EmployeeEntity;

public class EmployeeMapper {
    public static EmployeeEntity toEntity(Employee employee) {
        if (employee == null) return null;
        EmployeeEntity entity = new EmployeeEntity();
        entity.setIdEmpleado(employee.getIdEmpleado());
        entity.setDocument(employee.getDocument());
        entity.setFullName(employee.getFullName());
        entity.setPhone(employee.getPhone());
        entity.setUserName(employee.getUserName());
        entity.setPassword(employee.getPassword());
        if (employee.getRole() != null) {
            entity.setRole(employee.getRole().name());
        }
        return entity;
    }

    public static Employee toDomain(EmployeeEntity entity) {
        if (entity == null) return null;
        Employee employee = new Employee();
        employee.setIdEmpleado(entity.getIdEmpleado());
        employee.setDocument(entity.getDocument());
        employee.setFullName(entity.getFullName());
        employee.setPhone(entity.getPhone());
        employee.setUserName(entity.getUserName());
        employee.setPassword(entity.getPassword());
        if (entity.getRole() != null) {
            employee.setRole(Role.valueOf(entity.getRole()));
        }
        return employee;
    }
}