package app.domain.services;

import app.domain.exception.BusinessException;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateEmployee Unit Tests")
class CreateEmployeeTest {

    @Mock
    private EmployeePort employeePort;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CreateEmployee createEmployee;

    private Employee newEmployee;

    @BeforeEach
    void setUp() {
        newEmployee = new Employee();
        newEmployee.setDocument(123456789L);
        newEmployee.setFullName("Juan Pérez");
        newEmployee.setPassword("plainPassword123");
        newEmployee.setRole(Role.MESSENGER);
    }

    @Nested
    @DisplayName("Creación Exitosa")
    class SuccessfulCreationTests {

        @Test
        @DisplayName("Debe crear empleado con contraseña encriptada")
        void shouldCreateEmployeeWithEncodedPassword() throws Exception {
            when(employeePort.findByDocument(123456789L)).thenReturn(null);
            when(passwordEncoder.encode("plainPassword123")).thenReturn("$2a$10$encodedHash");
            when(employeePort.save(any())).thenReturn(newEmployee);

            createEmployee.create(newEmployee);

            verify(passwordEncoder).encode("plainPassword123");
            verify(employeePort).save(argThat(emp -> emp.getPassword().equals("$2a$10$encodedHash")));
        }

        @Test
        @DisplayName("Debe guardar empleado en el puerto")
        void shouldSaveEmployeeToPort() throws Exception {
            when(employeePort.findByDocument(anyLong())).thenReturn(null);
            when(passwordEncoder.encode(anyString())).thenReturn("encoded");
            when(employeePort.save(any())).thenReturn(newEmployee);

            createEmployee.create(newEmployee);

            verify(employeePort).save(newEmployee);
        }

        @Test
        @DisplayName("Debe permitir crear empleado sin contraseña")
        void shouldCreateEmployeeWithoutPassword() throws Exception {
            newEmployee.setPassword(null);
            when(employeePort.findByDocument(anyLong())).thenReturn(null);
            when(employeePort.save(any())).thenReturn(newEmployee);

            createEmployee.create(newEmployee);

            verify(passwordEncoder, never()).encode(anyString());
            verify(employeePort).save(newEmployee);
        }
    }

    @Nested
    @DisplayName("Validación de Documento")
    class DocumentValidationTests {

        @Test
        @DisplayName("Debe lanzar excepción si documento ya existe")
        void shouldThrowExceptionIfDocumentExists() {
            Employee existingEmployee = new Employee();
            existingEmployee.setDocument(123456789L);
            when(employeePort.findByDocument(123456789L)).thenReturn(existingEmployee);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> createEmployee.create(newEmployee));

            assertTrue(exception.getMessage().contains("Ya existe un empleado"));
            assertTrue(exception.getMessage().contains("123456789"));
            verify(employeePort, never()).save(any());
        }
    }
}
