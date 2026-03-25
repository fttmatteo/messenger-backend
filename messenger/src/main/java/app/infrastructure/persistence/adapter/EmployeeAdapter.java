package app.infrastructure.persistence.adapter;

import app.domain.model.Employee;
import app.domain.ports.EmployeePort;
import app.infrastructure.persistence.entities.EmployeeEntity;
import app.infrastructure.persistence.mapper.EmployeeMapper;
import app.infrastructure.persistence.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
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
    @Transactional
    public Employee save(Employee employee) {
        EmployeeEntity entity = mapper.toEntity(employee);
        EmployeeEntity savedEntity = repository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    /**
     * Busca empleado por ID.
     */
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
    public Employee findByDocument(Long document) {
        EmployeeEntity entity = repository.findByDocument(document);
        if (entity != null) {
            return mapper.toDomain(entity);
        }
        return null;
    }

    /**
     * Busca todos los empleados delegando al repositorio JPA.
     */
    @Override
    @Transactional(readOnly = true)
    public List<Employee> findAll() {
        return repository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    /**
     * Elimina un empleado por su ID delegando al repositorio JPA.
     */
    @Override
    public void deleteById(Long idEmployee) {
        repository.deleteById(idEmployee);
    }

    @Override
    /**
     * Busca empleado por UUID público.
     */
    @Transactional(readOnly = true)
    public Employee findByUuid(String uuid) {
        Optional<EmployeeEntity> entity = repository.findByUuid(uuid);
        return entity.map(mapper::toDomain).orElse(null);
    }
}
