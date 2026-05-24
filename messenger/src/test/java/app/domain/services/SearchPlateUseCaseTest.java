package app.domain.services;
import app.application.usecase.plate.SearchPlateUseCase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import app.domain.model.Plate;
import app.domain.ports.PlatePort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas unitarias de SearchPlateUseCase")
class SearchPlateTest {

    @Mock
    private PlatePort platePort;

    @InjectMocks
    private SearchPlateUseCase searchPlate;

    @Test
    @DisplayName("Debe buscar placa por ID")
    void shouldFindById() {
        Plate p = new Plate();
        p.setIdPlate(1L);
        when(platePort.findById(1L)).thenReturn(p);

        Plate result = searchPlate.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getIdPlate());
    }

    @Test
    @DisplayName("Debe buscar por número")

    void shouldFindByNumber() {
        Plate p = new Plate();
        p.setPlateNumber("ABC-123");
        when(platePort.findByPlateNumber("ABC-123")).thenReturn(p);

        Plate result = searchPlate.findByPlateNumber("ABC-123");

        assertNotNull(result);
        assertEquals("ABC-123", result.getPlateNumber());
    }
}
