package app.domain.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import app.domain.model.ServiceDelivery;
import app.domain.ports.ServiceDeliveryPort;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("SearchServiceDelivery Unit Tests")
class SearchServiceDeliveryTest {

    @Mock
    private ServiceDeliveryPort serviceDeliveryPort;

    @InjectMocks
    private SearchServiceDelivery searchServiceDelivery;

    @Test
    @DisplayName("Debe buscar servicio por ID")
    void shouldFindById() {
        ServiceDelivery s = new ServiceDelivery();
        s.setIdServiceDelivery(1L);
        when(serviceDeliveryPort.findById(1L)).thenReturn(s);

        ServiceDelivery result = searchServiceDelivery.findById(1L);

        assertEquals(1L, result.getIdServiceDelivery());
    }

    @Test
    @DisplayName("Debe buscar todos los servicios")
    void shouldFindAll() {
        when(serviceDeliveryPort.findAll()).thenReturn(Arrays.asList(new ServiceDelivery()));

        List<ServiceDelivery> result = searchServiceDelivery.findAll();

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Debe buscar servicios por placa")
    void shouldFindByPlateNumber() {
        when(serviceDeliveryPort.findByPlateNumber("ABC-123")).thenReturn(Arrays.asList(new ServiceDelivery()));

        List<ServiceDelivery> result = searchServiceDelivery.findByPlate("ABC-123");

        assertEquals(1, result.size());
    }
}
