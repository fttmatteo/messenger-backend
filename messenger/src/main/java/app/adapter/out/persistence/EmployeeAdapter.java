package app.adapter.out.persistence;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import app.domain.model.Employee;
import app.domain.ports.EmployeePort;
import app.infrastructure.persistence.entities.EmployeeEntity;
import app.infrastructure.persistence.mapper.EmployeeMapper;
import app.infrastructure.persistence.repository.EmployeeRepository;

@Service
public class EmployeeAdapter implements EmployeePort {
    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    public Employee findByDocument(Long document) {
        EmployeeEntity entity = employeeRepository.findByDocument(document);
        return EmployeeMapper.toDomain(entity);
    }

    @Override
    public Employee findByUserName(String userName) {
        EmployeeEntity entity = employeeRepository.findByUserName(userName);
        return EmployeeMapper.toDomain(entity);
    }

    @Override
    public Employee save(Employee employee) {
        EmployeeEntity entity = EmployeeMapper.toEntity(employee);
        EmployeeEntity saved = employeeRepository.save(entity);
        return EmployeeMapper.toDomain(saved);
    }

    @Override
    public void deleteByDocument(Long document) {
        EmployeeEntity entity = employeeRepository.findByDocument(document);
        if (entity != null) {
            employeeRepository.delete(entity);
        }
    }

    @Override
    public List<Employee> findAll() {
        List<EmployeeEntity> entities = employeeRepository.findAll();
        return entities.stream().map(EmployeeMapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public Employee findById(Long id) {
        return employeeRepository.findById(id)
                .map(EmployeeMapper::toDomain)
                .orElse(null);
    }

    @Override
    public void deleteById(Long id) {
        employeeRepository.findById(id).ifPresent(entity -> employeeRepository.delete(entity));
    }
}