package app.infrastructure.persistence.mapper;

import app.domain.model.Employee;
import app.infrastructure.persistence.entities.EmployeeEntity;
import org.springframework.stereotype.Component;

@Component
public class EmployeeMapper {

    public Employee toDomain(EmployeeEntity entity) {
        if (entity == null)
            return null;
        Employee model = new Employee();
        model.setIdEmployee(entity.getIdEmployee());
        model.setDocument(entity.getDocument());
        model.setFullName(entity.getFullName());
        model.setPhone(entity.getPhone());
        model.setUserName(entity.getUserName());
        model.setPassword(entity.getPassword());
        model.setRole(entity.getRole());
        return model;
    }

    public EmployeeEntity toEntity(Employee domain) {
        if (domain == null)
            return null;
        EmployeeEntity entity = new EmployeeEntity();
        entity.setIdEmployee(domain.getIdEmployee());
        entity.setDocument(domain.getDocument());
        entity.setFullName(domain.getFullName());
        entity.setPhone(domain.getPhone());
        entity.setUserName(domain.getUserName());
        entity.setPassword(domain.getPassword());
        entity.setRole(domain.getRole());
        return entity;
    }
}