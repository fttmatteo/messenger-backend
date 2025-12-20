package app.application.usecase;

import app.application.exceptions.BusinessException;
import app.application.exceptions.ResourceNotFoundException;
import app.domain.model.Employee;
import app.domain.model.enums.Role;
import app.domain.services.CreateEmployee;
import app.domain.services.DeleteEmployee;
import app.domain.services.SearchEmployee;
import app.domain.services.UpdateEmployee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para EmployeeUseCase.
 * 
 * Verifica la orquestación correcta de operaciones CRUD de empleados.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EmployeeUseCase Unit Tests")
class EmployeeUseCaseTest {

    @Mock
    private CreateEmployee createEmployee;

    @Mock
    private SearchEmployee searchEmployee;

    @Mock
    private UpdateEmployee updateEmployee;

    @Mock
    private DeleteEmployee deleteEmployee;

    @InjectMocks
    private EmployeeUseCase employeeUseCase;

    private Employee sampleEmployee;

    @BeforeEach
    void setUp() {
        sampleEmployee = new Employee();
        sampleEmployee.setDocument(123456789L);
        sampleEmployee.setFullName("Juan Pérez");
        sampleEmployee.setPhone("3001234567");
        sampleEmployee.setPassword("encoded_password");
        sampleEmployee.setRole(Role.MESSENGER);
    }

    @Nested
    @DisplayName("Crear Empleado")
    class CreateTests {

        @Test
        @DisplayName("Debe crear empleado exitosamente")
        void shouldCreateEmployeeSuccessfully() throws Exception {
            employeeUseCase.create(sampleEmployee);

            verify(createEmployee, times(1)).create(sampleEmployee);
        }

        @Test
        @DisplayName("Debe propagar excepción si documento duplicado")
        void shouldPropagateExceptionOnDuplicateDocument() throws Exception {
            doThrow(new BusinessException("Documento ya registrado"))
                    .when(createEmployee).create(any());

            assertThrows(BusinessException.class,
                    () -> employeeUseCase.create(sampleEmployee));
        }
    }

    @Nested
    @DisplayName("Buscar Empleados")
    class SearchTests {

        @Test
        @DisplayName("Debe retornar todos los empleados")
        void shouldReturnAllEmployees() {
            Employee employee2 = new Employee();
            employee2.setDocument(987654321L);
            employee2.setFullName("María García");
            employee2.setRole(Role.ADMIN);

            when(searchEmployee.findAll()).thenReturn(List.of(sampleEmployee, employee2));

            List<Employee> result = employeeUseCase.findAll();

            assertEquals(2, result.size());
            verify(searchEmployee).findAll();
        }

        @Test
        @DisplayName("Debe buscar empleado por ID")
        void shouldFindEmployeeById() {
            when(searchEmployee.findById(1L)).thenReturn(sampleEmployee);

            Employee result = employeeUseCase.findById(1L);

            assertNotNull(result);
            assertEquals("Juan Pérez", result.getFullName());
        }

        @Test
        @DisplayName("Debe buscar empleado por documento")
        void shouldFindEmployeeByDocument() throws Exception {
            when(searchEmployee.findByDocument(123456789L)).thenReturn(sampleEmployee);

            Employee result = employeeUseCase.findByDocument(123456789L);

            assertNotNull(result);
            assertEquals(123456789L, result.getDocument());
        }
    }

    @Nested
    @DisplayName("Actualizar Empleado")
    class UpdateTests {

        @Test
        @DisplayName("Debe actualizar empleado exitosamente")
        void shouldUpdateEmployeeSuccessfully() throws Exception {
            sampleEmployee.setFullName("Juan García");

            employeeUseCase.update(1L, sampleEmployee);

            verify(updateEmployee, times(1)).update(1L, sampleEmployee);
        }

        @Test
        @DisplayName("Debe propagar excepción si empleado no existe")
        void shouldPropagateExceptionIfNotFound() throws Exception {
            doThrow(new ResourceNotFoundException("Empleado no encontrado"))
                    .when(updateEmployee).update(anyLong(), any());

            assertThrows(ResourceNotFoundException.class,
                    () -> employeeUseCase.update(1L, sampleEmployee));
        }
    }

    @Nested
    @DisplayName("Eliminar Empleado")
    class DeleteTests {

        @Test
        @DisplayName("Debe eliminar empleado por ID sin servicios activos")
        void shouldDeleteEmployeeByIdWithoutActiveServices() throws Exception {
            employeeUseCase.deleteById(1L);

            verify(deleteEmployee, times(1)).deleteById(1L);
        }

        @Test
        @DisplayName("Debe lanzar excepción si tiene servicios activos")
        void shouldThrowExceptionIfHasActiveServices() throws Exception {
            doThrow(new BusinessException("Empleado tiene servicios asignados"))
                    .when(deleteEmployee).deleteById(1L);

            assertThrows(BusinessException.class,
                    () -> employeeUseCase.deleteById(1L));
        }
    }

    @Nested
    @DisplayName("Validaciones de Rol")
    class RoleValidationTests {

        @Test
        @DisplayName("Debe aceptar rol MESSENGER")
        void shouldAcceptMessengerRole() throws Exception {
            sampleEmployee.setRole(Role.MESSENGER);

            employeeUseCase.create(sampleEmployee);

            verify(createEmployee).create(argThat(e -> e.getRole() == Role.MESSENGER));
        }

        @Test
        @DisplayName("Debe aceptar rol ADMIN")
        void shouldAcceptAdminRole() throws Exception {
            sampleEmployee.setRole(Role.ADMIN);

            employeeUseCase.create(sampleEmployee);

            verify(createEmployee).create(argThat(e -> e.getRole() == Role.ADMIN));
        }
    }
}
