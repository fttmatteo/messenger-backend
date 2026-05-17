package app.domain.services;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import app.domain.model.Dealership;
import app.domain.ports.DealershipPort;
import app.domain.ports.ServiceDeliveryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas unitarias de DeleteDealership")
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
        deleteDealership.deleteById(1L);
        verify(dealershipPort).deleteById(1L);
    }
}
