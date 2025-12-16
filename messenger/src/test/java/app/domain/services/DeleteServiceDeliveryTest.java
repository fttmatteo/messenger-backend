package app.domain.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import app.application.exceptions.BusinessException;
import app.domain.model.Plate;
import app.domain.model.ServiceDelivery;
import app.domain.model.enums.Status;
import app.domain.ports.ServiceDeliveryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeleteServiceDelivery Unit Tests")
class DeleteServiceDeliveryTest {

    @Mock
    private ServiceDeliveryPort serviceDeliveryPort;

    @InjectMocks
    private DeleteServiceDelivery deleteServiceDelivery;

    @Test
    @DisplayName("Debe eliminar servicio si no está entregado")
    void shouldDeleteServiceIfNotDelivered() throws Exception {
        ServiceDelivery service = new ServiceDelivery();
        service.setIdServiceDelivery(1L);
        service.setCurrentStatus(Status.PENDING);
        Plate plate = new Plate();
        plate.setPlateNumber("ABC-123");
        service.setPlate(plate);

        when(serviceDeliveryPort.findById(1L)).thenReturn(service);

        deleteServiceDelivery.deleteById(1L);

        verify(serviceDeliveryPort).deleteById(1L);
    }

    @Test
    @DisplayName("Debe lanzar excepción si servicio está entregado")
    void shouldThrowExceptionIfServiceIsDelivered() {
        ServiceDelivery service = new ServiceDelivery();
        service.setIdServiceDelivery(1L);
        service.setCurrentStatus(Status.DELIVERED);

        when(serviceDeliveryPort.findById(1L)).thenReturn(service);

        BusinessException ex = assertThrows(BusinessException.class, () -> deleteServiceDelivery.deleteById(1L));

        assertEquals("El servicio de entrega que intenta eliminar ya está finalizado.", ex.getMessage());
        verify(serviceDeliveryPort, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Debe lanzar excepción si servicio no existe")
    void shouldThrowExceptionIfServiceNotFound() {
        when(serviceDeliveryPort.findById(1L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> deleteServiceDelivery.deleteById(1L));

        assertEquals("El servicio de entrega que intenta eliminar no existe.", ex.getMessage());
    }
}
