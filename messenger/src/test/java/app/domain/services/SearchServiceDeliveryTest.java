package app.domain.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import app.domain.exception.BusinessException;
import app.domain.model.ServiceDelivery;
import app.domain.ports.ServiceDeliveryPort;
import java.util.Arrays;
import java.util.Collections;
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
    @DisplayName("Debe buscar servicio activo por ID")
    void shouldFindActiveById() {
        ServiceDelivery s = new ServiceDelivery();
        s.setIdServiceDelivery(1L);
        when(serviceDeliveryPort.findByIdActive(1L)).thenReturn(s);

        ServiceDelivery result = searchServiceDelivery.findById(1L);

        assertEquals(1L, result.getIdServiceDelivery());
    }

    @Test
    @DisplayName("Debe lanzar excepción si servicio no existe o está en papelera")
    void shouldThrowExceptionIfServiceNotFoundOrDeleted() {
        when(serviceDeliveryPort.findByIdActive(99L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> searchServiceDelivery.findById(99L));

        assertEquals("El servicio con ID 99 no existe o está en la papelera.", ex.getMessage());
    }

    @Test
    @DisplayName("Debe buscar todos los servicios activos (excluyendo eliminados)")
    void shouldFindAllActive() {
        when(serviceDeliveryPort.findAllActive()).thenReturn(Arrays.asList(new ServiceDelivery()));

        List<ServiceDelivery> result = searchServiceDelivery.findAll();

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Debe buscar todos los servicios incluyendo eliminados")
    void shouldFindAllIncludingDeleted() {
        ServiceDelivery active = new ServiceDelivery();
        active.setIdServiceDelivery(1L);
        ServiceDelivery deleted = new ServiceDelivery();
        deleted.setIdServiceDelivery(2L);
        deleted.setDeleted(true);

        when(serviceDeliveryPort.findAll()).thenReturn(Arrays.asList(active, deleted));

        List<ServiceDelivery> result = searchServiceDelivery.findAllIncludingDeleted();

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Debe buscar servicios por placa")
    void shouldFindByPlateNumber() {
        when(serviceDeliveryPort.findByPlateNumber("ABC-123")).thenReturn(Arrays.asList(new ServiceDelivery()));

        List<ServiceDelivery> result = searchServiceDelivery.findByPlate("ABC-123");

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Debe retornar servicios eliminados (papelera)")
    void shouldFindDeleted() {
        ServiceDelivery deleted = new ServiceDelivery();
        deleted.setIdServiceDelivery(1L);
        deleted.setDeleted(true);

        when(serviceDeliveryPort.findDeleted()).thenReturn(Arrays.asList(deleted));

        List<ServiceDelivery> result = searchServiceDelivery.findDeleted();

        assertEquals(1, result.size());
        assertTrue(result.get(0).isDeleted());
    }

    @Test
    @DisplayName("Debe retornar lista vacía si no hay servicios en papelera")
    void shouldReturnEmptyListIfNoDeletedServices() {
        when(serviceDeliveryPort.findDeleted()).thenReturn(Collections.emptyList());

        List<ServiceDelivery> result = searchServiceDelivery.findDeleted();

        assertTrue(result.isEmpty());
    }
}
