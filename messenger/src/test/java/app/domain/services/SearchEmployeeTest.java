package app.domain.services;

import app.domain.model.Employee;
import app.domain.model.enums.Role;
import app.domain.ports.EmployeePort;
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

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas unitarias de SearchEmployee")
class SearchEmployeeTest {

    @Mock
    private EmployeePort employeePort;

    @InjectMocks
    private SearchEmployee searchEmployee;

    private Employee sampleEmployee;

    @BeforeEach
    void setUp() {
        sampleEmployee = new Employee();
        sampleEmployee.setIdEmployee(1L);
        sampleEmployee.setDocument(123456789L);
        sampleEmployee.setFullName("Juan Pérez");
        sampleEmployee.setRole(Role.MESSENGER);
    }

    @Nested
    @DisplayName("Buscar Todos")
    class FindAllTests {

        @Test
        @DisplayName("Debe retornar lista de empleados")
        void shouldReturnListOfEmployees() {
            Employee employee2 = new Employee();
            employee2.setDocument(987654321L);
            employee2.setFullName("María García");

            when(employeePort.findAll()).thenReturn(List.of(sampleEmployee, employee2));

            List<Employee> result = searchEmployee.findAll();

            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("Debe retornar lista vacía si no hay empleados")

        void shouldReturnEmptyListIfNoEmployees() {
            when(employeePort.findAll()).thenReturn(List.of());

            List<Employee> result = searchEmployee.findAll();

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Buscar por ID")
    class FindByIdTests {

        @Test
        @DisplayName("Debe retornar empleado para ID existente")
        void shouldReturnEmployeeForExistingId() {
            when(employeePort.findById(1L)).thenReturn(sampleEmployee);

            Employee result = searchEmployee.findById(1L);

            assertNotNull(result);
            assertEquals(1L, result.getIdEmployee());
        }

        @Test
        @DisplayName("Debe lanzar excepción para ID no existente")

        void shouldThrowExceptionForNonExistingId() {
            when(employeePort.findById(999L)).thenReturn(null);

            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> searchEmployee.findById(999L));

            assertTrue(exception.getMessage().contains("no existe"));
        }
    }

    @Nested
    @DisplayName("Buscar por Documento")
    class FindByDocumentTests {

        @Test
        @DisplayName("Debe retornar empleado para documento existente")
        void shouldReturnEmployeeForExistingDocument() throws Exception {
            when(employeePort.findByDocument(123456789L)).thenReturn(sampleEmployee);

            Employee result = searchEmployee.findByDocument(123456789L);

            assertNotNull(result);
            assertEquals(123456789L, result.getDocument());
        }

        @Test
        @DisplayName("Debe lanzar excepción para documento no existente")

        void shouldThrowExceptionForNonExistingDocument() {
            when(employeePort.findByDocument(999999999L)).thenReturn(null);

            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> searchEmployee.findByDocument(999999999L));

            assertTrue(exception.getMessage().contains("no existe"));
        }
    }
}
