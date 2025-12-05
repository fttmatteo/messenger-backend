package app.infrastructure.persistence.mapper;

import app.domain.model.Employee;
import app.domain.model.enums.Role;
import app.infrastructure.persistence.entities.EmployeeEntity;

public class EmployeeMapper {
    public static EmployeeEntity toEntity(Employee employee) {
        if (employee == null) return null;
        EmployeeEntity entity = new EmployeeEntity();
        entity.setId_empleado(employee.getId_empleado());
        entity.setDocument(employee.getDocument());
        entity.setFull_name(employee.getFull_name());
        entity.setPhone(employee.getPhone());
        entity.setUser_name(employee.getUser_name());
        entity.setPassword(employee.getPassword());
        if (employee.getRole() != null) {
            entity.setRole(employee.getRole().name());
        }
        if (employee.getZone() != null) {
            entity.setZone(employee.getZone().name());
        }
        return entity;
    }

    public static Employee toDomain(EmployeeEntity entity) {
        if (entity == null) return null;
        Employee employee = new Employee();
        employee.setId_empleado(entity.getId_empleado());
        employee.setDocument(entity.getDocument());
        employee.setFull_name(entity.getFull_name());
        employee.setPhone(entity.getPhone());
        employee.setUser_name(entity.getUser_name());
        employee.setPassword(entity.getPassword());
        if (entity.getRole() != null) {
            employee.setRole(Role.valueOf(entity.getRole()));
        }
        return employee;
    }
}