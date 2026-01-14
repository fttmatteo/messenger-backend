package app.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import app.domain.model.ServiceDelivery;
import app.domain.model.enums.Status;
import app.infrastructure.persistence.entities.ServiceDeliveryEntity;
import app.infrastructure.persistence.mapper.ServiceDeliveryMapper;
import app.infrastructure.persistence.repository.ServiceDeliveryRepository;
import app.infrastructure.persistence.adapter.ServiceDeliveryAdapter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("ServiceDeliveryAdapter Unit Tests")
class ServiceDeliveryAdapterTest {

    @Mock
    private ServiceDeliveryRepository repository;

    @Mock
    private ServiceDeliveryMapper mapper;

    @InjectMocks
    private ServiceDeliveryAdapter serviceDeliveryAdapter;

    @Test
    @DisplayName("Debe guardar y recuperar un servicio de entrega")
    /**
     * Verifica que el adaptador guarde y recupere servicios de entrega, manteniendo
     * el estado correcto.
     */
    void shouldSaveAndRetrieveServiceDelivery() {
        ServiceDelivery delivery = new ServiceDelivery();
        delivery.setCurrentStatus(Status.PENDING);

        ServiceDeliveryEntity entity = new ServiceDeliveryEntity();
        entity.setIdServiceDelivery(1L);

        ServiceDelivery savedDelivery = new ServiceDelivery();
        savedDelivery.setIdServiceDelivery(1L);
        savedDelivery.setCurrentStatus(Status.PENDING);

        when(mapper.toEntity(delivery)).thenReturn(entity);
        when(repository.save(any(ServiceDeliveryEntity.class))).thenReturn(entity);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(savedDelivery);

        ServiceDelivery saved = serviceDeliveryAdapter.save(delivery);

        verify(repository).save(any(ServiceDeliveryEntity.class));
        assertNotNull(saved.getIdServiceDelivery());

        ServiceDelivery found = serviceDeliveryAdapter.findById(1L);
        assertNotNull(found);
        assertEquals(Status.PENDING, found.getCurrentStatus());
    }

    @Test
    @DisplayName("Debe listar todos")
    /**
     * Verifica listado de todos los servicios.
     */
    void shouldFindAll() {
        ServiceDeliveryEntity e1 = new ServiceDeliveryEntity();
        ServiceDeliveryEntity e2 = new ServiceDeliveryEntity();

        ServiceDelivery s1 = new ServiceDelivery();
        ServiceDelivery s2 = new ServiceDelivery();

        when(repository.findAll()).thenReturn(Arrays.asList(e1, e2));
        when(mapper.toDomain(e1)).thenReturn(s1);
        when(mapper.toDomain(e2)).thenReturn(s2);

        List<ServiceDelivery> all = serviceDeliveryAdapter.findAll();

        assertEquals(2, all.size());
        verify(repository).findAll();
    }

    @Test
    @DisplayName("Debe retornar servicios paginados correctamente")
    /**
     * Verifica que findAllPaginated delegue correctamente al repositorio
     * y mapee los resultados.
     */
    void shouldFindAllPaginated() {
        ServiceDeliveryEntity entity = new ServiceDeliveryEntity();
        ServiceDelivery domain = new ServiceDelivery();

        org.springframework.data.domain.Page<ServiceDeliveryEntity> entityPage = new org.springframework.data.domain.PageImpl<>(
                Arrays.asList(entity));

        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);

        when(repository.findByDeleted(false, pageable)).thenReturn(entityPage);
        when(mapper.toDomain(entity)).thenReturn(domain);

        org.springframework.data.domain.Page<ServiceDelivery> result = serviceDeliveryAdapter.findAllPaginated(null,
                false, null, pageable);

        assertEquals(1, result.getContent().size());
        verify(repository).findByDeleted(false, pageable);
        verify(mapper).toDomain(entity);
    }

    @Test
    @DisplayName("Debe retornar servicios paginados por mensajero")
    /**
     * Verifica que findByMessengerPaginated delegue correctamente al repositorio.
     */
    void shouldFindByMessengerPaginated() {
        Long messengerId = 1L;
        ServiceDeliveryEntity entity = new ServiceDeliveryEntity();
        ServiceDelivery domain = new ServiceDelivery();

        org.springframework.data.domain.Page<ServiceDeliveryEntity> entityPage = new org.springframework.data.domain.PageImpl<>(
                Arrays.asList(entity));

        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);

        when(repository.findByMessenger_IdEmployeeAndDeleted(messengerId, false, pageable))
                .thenReturn(entityPage);
        when(mapper.toDomain(entity)).thenReturn(domain);

        org.springframework.data.domain.Page<ServiceDelivery> result = serviceDeliveryAdapter
                .findByMessengerPaginated(messengerId, null, false, null, pageable);

        assertEquals(1, result.getContent().size());
        verify(repository).findByMessenger_IdEmployeeAndDeleted(messengerId, false, pageable);
        verify(mapper).toDomain(entity);
    }
}
