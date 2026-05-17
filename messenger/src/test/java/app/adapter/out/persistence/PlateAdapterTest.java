package app.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import app.domain.model.Plate;
import app.domain.model.enums.PlateType;
import app.infrastructure.persistence.entities.PlateEntity;
import app.infrastructure.persistence.mapper.PlateMapper;
import app.infrastructure.persistence.repository.PlateRepository;
import app.infrastructure.persistence.adapter.PlateAdapter;
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
@DisplayName("Pruebas unitarias de PlateAdapter")
class PlateAdapterTest {

    @Mock
    private PlateRepository repository;

    @Mock
    private PlateMapper mapper;

    @InjectMocks
    private PlateAdapter plateAdapter;

    @Test
    @DisplayName("Debe guardar y recuperar una placa")
    void shouldSaveAndRetrievePlate() {
        Plate plate = new Plate();
        plate.setPlateNumber("ABC-123");
        plate.setPlateType(PlateType.MOTORCYCLE);

        PlateEntity entity = new PlateEntity();
        entity.setIdPlate(1L);
        entity.setPlateNumber("ABC-123");

        when(mapper.toEntity(plate)).thenReturn(entity);
        when(repository.save(any(PlateEntity.class))).thenReturn(entity);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(plate);

        plateAdapter.save(plate);

        verify(repository).save(any(PlateEntity.class));
        assertNotNull(plate.getIdPlate());

        Plate found = plateAdapter.findById(1L);
        assertNotNull(found);
        assertEquals("ABC-123", found.getPlateNumber());
    }

    @Test
    @DisplayName("Debe buscar por número de placa")

    void shouldFindByPlateNumber() {
        PlateEntity entity = new PlateEntity();
        entity.setPlateNumber("XYZ-789");

        Plate plate = new Plate();
        plate.setPlateNumber("XYZ-789");
        plate.setPlateType(PlateType.MOTORCYCLE);

        when(repository.findByPlateNumber("XYZ-789")).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(plate);

        Plate found = plateAdapter.findByPlateNumber("XYZ-789");

        assertNotNull(found);
        assertEquals(PlateType.MOTORCYCLE, found.getPlateType());
        verify(repository).findByPlateNumber("XYZ-789");
    }

    @Test
    @DisplayName("Debe buscar todos")

    void shouldFindAll() {
        PlateEntity e1 = new PlateEntity();
        e1.setPlateNumber("P1");
        PlateEntity e2 = new PlateEntity();
        e2.setPlateNumber("P2");

        Plate p1 = new Plate();
        p1.setPlateNumber("P1");
        Plate p2 = new Plate();
        p2.setPlateNumber("P2");

        when(repository.findAll()).thenReturn(Arrays.asList(e1, e2));
        when(mapper.toDomain(e1)).thenReturn(p1);
        when(mapper.toDomain(e2)).thenReturn(p2);

        List<Plate> all = plateAdapter.findAll();

        assertEquals(2, all.size());
        verify(repository).findAll();
    }
}
