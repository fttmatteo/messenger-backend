package app.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import app.domain.model.Employee;
import app.domain.model.enums.Role;
import app.infrastructure.persistence.entities.EmployeeEntity;
import app.infrastructure.persistence.mapper.EmployeeMapper;
import app.infrastructure.persistence.repository.EmployeeRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmployeeAdapter Unit Tests")
class EmployeeAdapterTest {

    @Mock
    private EmployeeRepository repository;

    @Mock
    private EmployeeMapper mapper;

    @InjectMocks
    private EmployeeAdapter employeeAdapter;

    @Test
    @DisplayName("Debe guardar y recuperar un empleado")
    void shouldSaveAndRetrieveEmployee() {
        Employee employee = new Employee();
        employee.setFullName("John Doe");
        employee.setRole(Role.ADMIN);

        EmployeeEntity entity = new EmployeeEntity();
        entity.setIdEmployee(1L);
        entity.setFullName("John Doe");

        when(mapper.toEntity(employee)).thenReturn(entity);
        when(repository.save(any(EmployeeEntity.class))).thenReturn(entity);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(employee);

        employeeAdapter.save(employee);

        verify(repository).save(any(EmployeeEntity.class));
        assertNotNull(employee.getIdEmployee());

        Employee found = employeeAdapter.findById(1L);
        assertNotNull(found);
        assertEquals("John Doe", found.getFullName());
    }

    @Test
    @DisplayName("Debe buscar por nombre de usuario")
    void shouldFindByUserName() {
        EmployeeEntity entity = new EmployeeEntity();
        entity.setUserName("testuser");

        Employee employee = new Employee();
        employee.setUserName("testuser");

        when(repository.findByUserName("testuser")).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(employee);

        Employee found = employeeAdapter.findByUserName("testuser");

        assertNotNull(found);
        assertEquals("testuser", found.getUserName());
        verify(repository).findByUserName("testuser");
    }

    @Test
    @DisplayName("Debe listar todos")
    void shouldFindAll() {
        EmployeeEntity e1 = new EmployeeEntity();
        e1.setFullName("E1");
        EmployeeEntity e2 = new EmployeeEntity();
        e2.setFullName("E2");

        Employee emp1 = new Employee();
        emp1.setFullName("E1");
        Employee emp2 = new Employee();
        emp2.setFullName("E2");

        when(repository.findAll()).thenReturn(Arrays.asList(e1, e2));
        when(mapper.toDomain(e1)).thenReturn(emp1);
        when(mapper.toDomain(e2)).thenReturn(emp2);

        List<Employee> all = employeeAdapter.findAll();

        assertEquals(2, all.size());
        verify(repository).findAll();
    }
}
