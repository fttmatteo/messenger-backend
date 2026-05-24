package app.domain.services;
import app.application.usecase.delivery.SearchServiceDelivery;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import app.domain.exception.BusinessException;
import app.domain.model.ServiceDelivery;
import app.domain.ports.ServiceDeliveryPort;
import java.util.Arrays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas unitarias de SearchServiceDelivery")
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
    @DisplayName("Debe lanzar excepción si el servicio no existe o está eliminado")

    void shouldThrowExceptionIfServiceNotFoundOrDeleted() {
        when(serviceDeliveryPort.findByIdActive(99L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> searchServiceDelivery.findById(99L));

        assertEquals("El servicio no existe o está en la papelera.", ex.getMessage());
    }

    @Test
    @DisplayName("Debe buscar incluyendo eliminados por UUID")

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
    @DisplayName("Debe buscar por placa/chasis y concesionario de forma paginada")

    void shouldFindByPlateAndDealershipPaginated() {
        when(serviceDeliveryPort.findByPlateAndDealershipPaginated(eq("ABC-123"), eq(1L), any()))
                .thenReturn(new PageImpl<>(Arrays.asList(new ServiceDelivery())));

        Page<ServiceDelivery> result = searchServiceDelivery.findByPlateAndDealershipPaginated("ABC-123", 1L,
                PageRequest.of(0, 10));

        assertEquals(1, result.getContent().size());
    }

    @Test
    @DisplayName("Debe buscar eliminados")

    void shouldFindDeleted() {
        ServiceDelivery deleted = new ServiceDelivery();
        deleted.setIdServiceDelivery(1L);
        deleted.setDeleted(true);
        Page<ServiceDelivery> page = new PageImpl<>(Arrays.asList(deleted));

        when(serviceDeliveryPort.findDeleted(any(Pageable.class))).thenReturn(page);

        Page<ServiceDelivery> result = searchServiceDelivery.findDeleted(PageRequest.of(0, 10));

        assertEquals(1, result.getContent().size());
        assertTrue(result.getContent().get(0).isDeleted());
    }

    @Test
    @DisplayName("Debe retornar página vacía si no hay servicios eliminados")

    void shouldReturnEmptyPageIfNoDeletedServices() {
        when(serviceDeliveryPort.findDeleted(any(Pageable.class))).thenReturn(Page.empty());

        Page<ServiceDelivery> result = searchServiceDelivery.findDeleted(PageRequest.of(0, 10));

        assertTrue(result.isEmpty());
    }
}
