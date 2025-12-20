package app.domain.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import app.application.exceptions.BusinessException;
import app.domain.model.Dealership;
import app.domain.model.ServiceDelivery;
import app.domain.ports.DealershipPort;
import app.domain.ports.ServiceDeliveryPort;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeleteDealership Unit Tests")
class DeleteDealershipTest {

    @Mock
    private DealershipPort dealershipPort;
    @Mock
    private ServiceDeliveryPort serviceDeliveryPort;

    @InjectMocks
    private DeleteDealership deleteDealership;

    @Test
    @DisplayName("Debe eliminar concesionario por ID si no tiene servicios")
    void shouldDeleteByIdIfNoServices() throws Exception {
        Dealership d = new Dealership();
        d.setIdDealership(1L);
        when(dealershipPort.findById(1L)).thenReturn(d);
        when(serviceDeliveryPort.findByDealershipId(1L)).thenReturn(Collections.emptyList());

        deleteDealership.deleteById(1L);

        verify(dealershipPort).deleteById(1L);
    }

    @Test
    @DisplayName("Debe lanzar excepción por ID si tiene servicios")
    void shouldThrowExceptionByIdIfHasServices() {
        Dealership d = new Dealership();
        d.setIdDealership(1L);
        when(dealershipPort.findById(1L)).thenReturn(d);
        when(serviceDeliveryPort.findByDealershipId(1L)).thenReturn(List.of(new ServiceDelivery()));

        BusinessException ex = assertThrows(BusinessException.class, () -> deleteDealership.deleteById(1L));

        assertEquals("No se puede eliminar el concesionario porque tiene servicios activos asociados.",
                ex.getMessage());
        verify(dealershipPort, never()).deleteById(anyLong());
    }
}
