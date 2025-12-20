package app.domain.services;

import app.application.exceptions.BusinessException;
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

/**
 * Tests unitarios para CreateEmployee.
 * 
 * Verifica la creación de empleados incluyendo validaciones de unicidad
 * y encriptación de contraseñas.
 */
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
        newEmployee.setUserName("jperez");
        newEmployee.setPassword("plainPassword123");
        newEmployee.setRole(Role.MESSENGER);
    }

    @Nested
    @DisplayName("Creación Exitosa")
    class SuccessfulCreationTests {

        @Test
        @DisplayName("Debe crear empleado con contraseña encriptada")
        void shouldCreateEmployeeWithEncodedPassword() throws Exception {
            when(employeePort.existsByDocument(123456789L)).thenReturn(false);
            when(employeePort.findByUserName("jperez")).thenReturn(null);
            when(passwordEncoder.encode("plainPassword123")).thenReturn("$2a$10$encodedHash");
            when(employeePort.save(any())).thenReturn(newEmployee);

            createEmployee.create(newEmployee);

            verify(passwordEncoder).encode("plainPassword123");
            verify(employeePort).save(argThat(emp -> emp.getPassword().equals("$2a$10$encodedHash")));
        }

        @Test
        @DisplayName("Debe guardar empleado en el puerto")
        void shouldSaveEmployeeToPort() throws Exception {
            when(employeePort.existsByDocument(anyLong())).thenReturn(false);
            when(employeePort.findByUserName(anyString())).thenReturn(null);
            when(passwordEncoder.encode(anyString())).thenReturn("encoded");
            when(employeePort.save(any())).thenReturn(newEmployee);

            createEmployee.create(newEmployee);

            verify(employeePort).save(newEmployee);
        }

        @Test
        @DisplayName("Debe permitir crear empleado sin contraseña")
        void shouldCreateEmployeeWithoutPassword() throws Exception {
            newEmployee.setPassword(null);
            when(employeePort.existsByDocument(anyLong())).thenReturn(false);
            when(employeePort.findByUserName(anyString())).thenReturn(null);
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
            when(employeePort.existsByDocument(123456789L)).thenReturn(true);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> createEmployee.create(newEmployee));

            assertTrue(exception.getMessage().contains("Ya existe un empleado"));
            assertTrue(exception.getMessage().contains("123456789"));
            verify(employeePort, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Validación de Username")
    class UsernameValidationTests {

        @Test
        @DisplayName("Debe lanzar excepción si username ya existe")
        void shouldThrowExceptionIfUsernameExists() {
            when(employeePort.existsByDocument(anyLong())).thenReturn(false);

            Employee existingEmployee = new Employee();
            existingEmployee.setUserName("jperez");
            when(employeePort.findByUserName("jperez")).thenReturn(existingEmployee);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> createEmployee.create(newEmployee));

            assertTrue(exception.getMessage().contains("ya está en uso"));
            assertTrue(exception.getMessage().contains("jperez"));
            verify(employeePort, never()).save(any());
        }
    }
}
