package app.adapter.out.persistence;

import app.domain.model.Employee;
import app.domain.ports.EmployeePort;
import app.infrastructure.persistence.entities.EmployeeEntity;
import app.infrastructure.persistence.mapper.EmployeeMapper;
import app.infrastructure.persistence.repository.EmployeeRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class EmployeeAdapter implements EmployeePort {

    @Autowired
    private EmployeeRepository repository;
    @Autowired
    private EmployeeMapper mapper;

    @Override
    public void save(Employee employee) {
        EmployeeEntity entity = mapper.toEntity(employee);
        EmployeeEntity savedEntity = repository.save(entity);
        employee.setIdEmployee(savedEntity.getIdEmployee());
    }

    @Override
    public Employee findById(Long idEmployee) {
        Optional<EmployeeEntity> entity = repository.findById(idEmployee);
        if (entity.isPresent()) {
            return mapper.toDomain(entity.get());
        }
        return null;
    }

    @Override
    public Employee findByDocument(Long document) {
        EmployeeEntity entity = repository.findByDocument(document);
        if (entity != null) {
            return mapper.toDomain(entity);
        }
        return null;
    }

    @Override
    public Employee findByUserName(String userName) {
        EmployeeEntity entity = repository.findByUserName(userName);
        if (entity != null) {
            return mapper.toDomain(entity);
        }
        return null;
    }

    @Override
    public List<Employee> findAll() {
        return repository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long idEmployee) {
        repository.deleteById(idEmployee);
    }

    @Override
    public void deleteByDocument(Long document) {
        EmployeeEntity entity = repository.findByDocument(document);
        if (entity != null) {
            repository.delete(entity);
        }
    }
}