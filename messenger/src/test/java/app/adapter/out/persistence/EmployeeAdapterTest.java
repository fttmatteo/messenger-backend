package app.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import app.domain.model.Employee;
import app.domain.model.enums.Role;
import app.adapter.out.persistence.entities.EmployeeEntity;
import app.adapter.out.persistence.mapper.EmployeeMapper;
import app.adapter.out.persistence.repository.EmployeeRepository;
import app.adapter.out.persistence.adapter.EmployeeAdapter;
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
@DisplayName("Pruebas unitarias de EmployeeAdapter")
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

        Employee savedEmployee = new Employee();
        savedEmployee.setIdEmployee(1L);
        savedEmployee.setFullName("John Doe");
        savedEmployee.setRole(Role.ADMIN);

        when(mapper.toEntity(employee)).thenReturn(entity);
        when(repository.save(any(EmployeeEntity.class))).thenReturn(entity);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(savedEmployee);

        Employee saved = employeeAdapter.save(employee);

        verify(repository).save(any(EmployeeEntity.class));
        assertNotNull(saved.getIdEmployee());

        Employee found = employeeAdapter.findById(1L);
        assertNotNull(found);
        assertEquals("John Doe", found.getFullName());
    }

    @Test
    @DisplayName("Debe buscar por documento")

    void shouldFindByDocument() {
        EmployeeEntity entity = new EmployeeEntity();
        entity.setDocument(123456789L);

        Employee employee = new Employee();
        employee.setDocument(123456789L);

        when(repository.findByDocument(123456789L)).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(employee);

        Employee found = employeeAdapter.findByDocument(123456789L);

        assertNotNull(found);
        assertEquals(123456789L, found.getDocument());
        verify(repository).findByDocument(123456789L);
    }

    @Test
    @DisplayName("Debe buscar todos")

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
