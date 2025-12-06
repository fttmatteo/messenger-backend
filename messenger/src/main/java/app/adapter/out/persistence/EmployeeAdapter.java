package app.adapter.out.persistence;

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
    public Employee findByDocument(Employee employee) throws Exception {
        EmployeeEntity entity = employeeRepository.findByDocument(employee.getDocument());
        return EmployeeMapper.toDomain(entity);
    }

    @Override
    public Employee findByUserName(Employee employee) throws Exception {
        EmployeeEntity entity = employeeRepository.findByUserName(employee.getUserName());
        return EmployeeMapper.toDomain(entity);
    }

    @Override
    public void save(Employee employee) throws Exception {
        EmployeeEntity entity = EmployeeMapper.toEntity(employee);
        employeeRepository.save(entity);
        employee.setIdEmployee(entity.getIdEmployee());
    }

    @Override
    public void deleteByDocument(Long document) throws Exception {
        EmployeeEntity entity = employeeRepository.findByDocument(document);
        if (entity != null) {
            employeeRepository.delete(entity);
        }
    }

}