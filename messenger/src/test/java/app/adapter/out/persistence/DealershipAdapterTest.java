package app.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import app.domain.model.Dealership;
import app.adapter.out.persistence.entities.DealershipEntity;
import app.adapter.out.persistence.mapper.DealershipMapper;
import app.adapter.out.persistence.repository.DealershipRepository;
import app.adapter.out.persistence.adapter.DealershipAdapter;
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
@DisplayName("Pruebas unitarias de DealershipAdapter")
class DealershipAdapterTest {

    @Mock
    private DealershipRepository repository;

    @Mock
    private DealershipMapper mapper;

    @InjectMocks
    private DealershipAdapter dealershipAdapter;

    @Test
    @DisplayName("Debe guardar y recuperar un concesionario")
    void shouldSaveAndRetrieveDealership() {
        Dealership dealership = new Dealership();
        dealership.setName("Test Dealer");

        DealershipEntity entity = new DealershipEntity();
        entity.setIdDealership(1L);
        entity.setName("Test Dealer");

        Dealership savedDealership = new Dealership();
        savedDealership.setIdDealership(1L);
        savedDealership.setName("Test Dealer");

        when(mapper.toEntity(dealership)).thenReturn(entity);
        when(repository.save(any(DealershipEntity.class))).thenReturn(entity);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(savedDealership);

        Dealership saved = dealershipAdapter.save(dealership);

        verify(repository).save(any(DealershipEntity.class));
        assertNotNull(saved.getIdDealership());

        Dealership found = dealershipAdapter.findById(1L);
        assertNotNull(found);
        assertEquals("Test Dealer", found.getName());
    }

    @Test
    @DisplayName("Debe buscar por nombre")

    void shouldFindByName() {
        DealershipEntity entity = new DealershipEntity();
        entity.setName("Unique Name");

        Dealership dealership = new Dealership();
        dealership.setName("Unique Name");

        when(repository.findByName("Unique Name")).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(dealership);

        Dealership found = dealershipAdapter.findByName("Unique Name");

        assertNotNull(found);
        assertEquals("Unique Name", found.getName());
        verify(repository).findByName("Unique Name");
    }

    @Test
    @DisplayName("Debe buscar todos")

    void shouldFindAll() {
        DealershipEntity e1 = new DealershipEntity();
        e1.setName("D1");
        DealershipEntity e2 = new DealershipEntity();
        e2.setName("D2");

        Dealership d1 = new Dealership();
        d1.setName("D1");
        Dealership d2 = new Dealership();
        d2.setName("D2");

        when(repository.findAll()).thenReturn(Arrays.asList(e1, e2));
        when(mapper.toDomain(e1)).thenReturn(d1);
        when(mapper.toDomain(e2)).thenReturn(d2);

        List<Dealership> all = dealershipAdapter.findAll();

        assertEquals(2, all.size());
        verify(repository).findAll();
    }
}
