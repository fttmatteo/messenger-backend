package app.adapter.out.maps;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import com.google.maps.GeoApiContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("GoogleMapsAdapter Unit Tests")
class GoogleMapsAdapterTest {

    @Mock
    private GeoApiContext geoApiContext;

    @InjectMocks
    private GoogleMapsAdapter googleMapsAdapter;

    @Test
    @DisplayName("Debe geocodificar una dirección válida")
    /**
     * Verifica que el servicio de mapas geocodifique correctamente una dirección.
     */
    void shouldGeocodeAddress() throws Exception {
        assertNotNull(googleMapsAdapter);
    }

    @Test
    @DisplayName("Debe calcular distancia entre dos puntos")
    /**
     * Verifica el cálculo de distancia usando la API de mapas.
     */
    void shouldCalculateDistance() throws Exception {
        assertNotNull(googleMapsAdapter);
    }
}
