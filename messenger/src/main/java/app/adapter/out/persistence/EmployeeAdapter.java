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

/**
 * Adaptador de salida para persistencia de empleados.
 * 
 * Este adaptador implementa EmployeePort y actúa como puente entre la capa de
 * dominio
 * y la capa de infraestructura (JPA), manejando la conversión entre objetos de
 * dominio
 * (Employee) y entidades de persistencia (EmployeeEntity).
 * 
 * Responsabilidades:
 * - Convertir objetos de dominio a entidades JPA y viceversa usando
 * EmployeeMapper
 * - Delegar operaciones de persistencia al EmployeeRepository
 * - Mantener la independencia del dominio respecto a detalles de persistencia
 * 
 * Operaciones soportadas:
 * - save: Guardar o actualizar un empleado
 * - findById: Buscar por ID
 * - existsByDocument: Verificar existencia por documento de identidad
 * - findByUserName: Buscar por nombre de usuario (para autenticación)
 * - findAll: Obtener todos los empleados
 * - deleteById: Eliminar por ID
 * 
 * @see app.domain.ports.EmployeePort
 * @see app.infrastructure.persistence.repository.EmployeeRepository
 * @see app.infrastructure.persistence.mapper.EmployeeMapper
 */
@Component
public class EmployeeAdapter implements EmployeePort {

    @Autowired
    private EmployeeRepository repository;
    @Autowired
    private EmployeeMapper mapper;

    @Override
    public Employee save(Employee employee) {
        EmployeeEntity entity = mapper.toEntity(employee);
        EmployeeEntity savedEntity = repository.save(entity);
        return mapper.toDomain(savedEntity);
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
    public boolean existsByDocument(Long document) {
        return repository.existsByDocument(document);
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
}