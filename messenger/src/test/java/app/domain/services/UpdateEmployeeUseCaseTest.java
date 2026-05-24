package app.domain.services;
import app.application.usecase.employee.UpdateEmployeeUseCase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import app.domain.exception.BusinessException;
import app.domain.model.Employee;
import app.domain.model.enums.Role;
import app.domain.ports.EmployeePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas unitarias de UpdateEmployeeUseCase")
class UpdateEmployeeTest {

    @Mock
    private EmployeePort employeePort;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UpdateEmployeeUseCase updateEmployee;

    private Employee existingEmployee;

    @BeforeEach
    void setUp() {
        existingEmployee = new Employee();
        existingEmployee.setIdEmployee(1L);
        existingEmployee.setDocument(123L);
        existingEmployee.setPassword("oldPass");
        existingEmployee.setFullName("Juan");
        existingEmployee.setPhone("111");
        existingEmployee.setRole(Role.MESSENGER);
    }

    @Test
    @DisplayName("Debe actualizar campos y contraseña")

    void shouldUpdateFieldsAndPassword() throws Exception {
        Employee income = new Employee();
        income.setDocument(999L);
        income.setFullName("Pedro");
        income.setPhone("222");
        income.setRole(Role.ADMIN);
        income.setPassword("newPass");

        when(employeePort.findById(1L)).thenReturn(existingEmployee);
        when(employeePort.findByDocument(999L)).thenReturn(null);
        when(passwordEncoder.encode("newPass")).thenReturn("encodedNewPass");
        when(employeePort.save(any())).thenReturn(existingEmployee);

        updateEmployee.update(1L, income);

        verify(employeePort).save(argThat(e -> e.getDocument().equals(999L) &&
                e.getPassword().equals("encodedNewPass") &&
                e.getFullName().equals("Pedro")));
    }

    @Test
    @DisplayName("Debe lanzar excepción si el documento ya existe")

    void shouldThrowExceptionIfDocumentExists() {
        Employee income = new Employee();
        income.setDocument(999L);

        Employee otherEmployee = new Employee();
        otherEmployee.setDocument(999L);

        when(employeePort.findById(1L)).thenReturn(existingEmployee);
        when(employeePort.findByDocument(999L)).thenReturn(otherEmployee);

        BusinessException ex = assertThrows(BusinessException.class, () -> updateEmployee.update(1L, income));

        assertEquals("Ese documento ya está registrado por otro empleado.", ex.getMessage());
    }
}
