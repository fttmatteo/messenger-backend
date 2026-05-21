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
@DisplayName("Pruebas unitarias de DeleteEmployee")
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
        when(serviceDeliveryPort.findByMessengerPaginated(1L, null, null, null,
                org.springframework.data.domain.PageRequest.of(0, 1)))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(java.util.Collections.emptyList()));

        deleteEmployee.deleteById(1L);

        verify(employeePort).deleteById(1L);
    }

    @Test
    @DisplayName("Debe lanzar excepción por ID si tiene servicios asociados")

    void shouldThrowExceptionByIdIfHasServices() {
        Employee e = new Employee();
        e.setIdEmployee(1L);
        e.setDocument(123L);
        when(employeePort.findById(1L)).thenReturn(e);
        when(serviceDeliveryPort.findByMessengerPaginated(1L, null, null, null,
                org.springframework.data.domain.PageRequest.of(0, 1)))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(java.util.List.of(new ServiceDelivery()),
                        org.springframework.data.domain.PageRequest.of(0, 1), 1));

        BusinessException ex = assertThrows(BusinessException.class, () -> deleteEmployee.deleteById(1L));

        assertEquals("No se puede eliminar. El empleado tiene servicios de entrega asociados.", ex.getMessage());
        verify(employeePort, never()).deleteById(anyLong());
    }
}
