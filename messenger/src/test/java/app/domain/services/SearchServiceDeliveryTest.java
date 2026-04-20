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
    /**
     * Verifica recuperación de servicio activo (no eliminado) por ID.
     */
    void shouldFindActiveById() {
        ServiceDelivery s = new ServiceDelivery();
        s.setIdServiceDelivery(1L);
        when(serviceDeliveryPort.findByIdActive(1L)).thenReturn(s);

        ServiceDelivery result = searchServiceDelivery.findById(1L);

        assertEquals(1L, result.getIdServiceDelivery());
    }

    @Test
    @DisplayName("Debe lanzar excepción si servicio no existe o está en papelera")
    /**
     * Verifica que se lance excepción cuando el servicio no existe o está en la papelera.
     */
    void shouldThrowExceptionIfServiceNotFoundOrDeleted() {
        when(serviceDeliveryPort.findByIdActive(99L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> searchServiceDelivery.findById(99L));

        assertEquals("El servicio con ID 99 no existe o está en la papelera.", ex.getMessage());
    }

    @Test
    @DisplayName("Debe buscar servicio incluyendo eliminados por UUID")
    /**
     * Verifica recuperación de servicio por UUID incluso si está eliminado.
     */
    void shouldFindIncludingDeletedByUuid() throws BusinessException {
        ServiceDelivery s = new ServiceDelivery();
        s.setUuid("uuid-123");
        s.setDeleted(true);
        when(serviceDeliveryPort.findByUuidIncludingDeleted("uuid-123")).thenReturn(s);

        ServiceDelivery result = searchServiceDelivery.findByUuidIncludingDeleted("uuid-123");

        assertEquals("uuid-123", result.getUuid());
        assertTrue(result.isDeleted());
    }

    @Test
    @DisplayName("Debe buscar servicios por placa")
    /**
     * Verifica la búsqueda de servicios por número de placa.
     */
    void shouldFindByPlateNumber() {
        when(serviceDeliveryPort.findByPlateNumber("ABC-123")).thenReturn(Arrays.asList(new ServiceDelivery()));

        List<ServiceDelivery> result = searchServiceDelivery.findByPlate("ABC-123");

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Debe retornar servicios eliminados (papelera)")
    /**
     * Verifica que se retornen solo los servicios marcados como eliminados.
     */
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
    /**
     * Verifica que se retorne lista vacía cuando no hay servicios en la papelera.
     */
    void shouldReturnEmptyListIfNoDeletedServices() {
        when(serviceDeliveryPort.findDeleted()).thenReturn(Collections.emptyList());

        List<ServiceDelivery> result = searchServiceDelivery.findDeleted();

        assertTrue(result.isEmpty());
    }
}
