package app.domain.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import app.domain.exception.BusinessException;
import app.domain.model.Employee;
import app.domain.model.ServiceDelivery;
import app.domain.ports.EmployeePort;
import app.domain.ports.ServiceDeliveryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeleteEmployee Unit Tests")
class DeleteEmployeeTest {

    @Mock
    private EmployeePort employeePort;
    @Mock
    private ServiceDeliveryPort serviceDeliveryPort;

    @InjectMocks
    private DeleteEmployee deleteEmployee;

    @Test
    @DisplayName("Debe eliminar empleado por ID si no tiene servicios")
    void shouldDeleteByIdIfNoServices() throws Exception {
        Employee e = new Employee();
        e.setIdEmployee(1L);
        e.setDocument(123L);
        when(employeePort.findById(1L)).thenReturn(e);
        when(serviceDeliveryPort.findById(123L)).thenReturn(null);

        deleteEmployee.deleteById(1L);

        verify(employeePort).deleteById(1L);
    }

    @Test
    @DisplayName("Debe lanzar excepción por ID si tiene servicios")
    void shouldThrowExceptionByIdIfHasServices() {
        Employee e = new Employee();
        e.setIdEmployee(1L);
        e.setDocument(123L);
        when(employeePort.findById(1L)).thenReturn(e);
        when(serviceDeliveryPort.findById(123L)).thenReturn(new ServiceDelivery());

        BusinessException ex = assertThrows(BusinessException.class, () -> deleteEmployee.deleteById(1L));

        assertEquals("El empleado con ID 1 tiene servicios de entrega asociados.", ex.getMessage());
        verify(employeePort, never()).deleteById(anyLong());
    }
}
