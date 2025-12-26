package app.infrastructure.persistence.adapter;

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

/**
 * Adapter de persistencia para empleados.
 */
@Component
public class EmployeeAdapter implements EmployeePort {

    @Autowired
    private EmployeeRepository repository;
    @Autowired
    private EmployeeMapper mapper;

    @Override
    /**
     * Persiste un empleado transformándolo a entidad JPA.
     */
    public Employee save(Employee employee) {
        EmployeeEntity entity = mapper.toEntity(employee);
        EmployeeEntity savedEntity = repository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    /**
     * Busca empleado por ID.
     */
    public Employee findById(Long idEmployee) {
        Optional<EmployeeEntity> entity = repository.findById(idEmployee);
        if (entity.isPresent()) {
            return mapper.toDomain(entity.get());
        }
        return null;
    }

    @Override
    /**
     * Busca empleado por número de documento.
     */
    public Employee findByDocument(Long document) {
        EmployeeEntity entity = repository.findByDocument(document);
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
}
