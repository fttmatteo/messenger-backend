package app.application.usecase.location;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import app.domain.model.Dealership;
import app.domain.model.Location;
import app.domain.ports.DealershipPort;
import app.domain.ports.LocationPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("GeocodeDealershipUseCase Unit Tests")
class GeocodeDealershipUseCaseTest {

    @Mock
    private LocationPort locationPort;
    @Mock
    private DealershipPort dealershipPort;

    @InjectMocks
    private GeocodeDealershipUseCase geocodeDealership;

    @Test
    @DisplayName("Debe geocodificar y actualizar concesionario")
    /**
     * Verifica que se actualicen las coordenadas del concesionario usando el
     * servicio de geocodificación.
     */
    void shouldGeocodeAndUpdateDealership() {
        Dealership d = new Dealership();
        d.setIdDealership(1L);
        d.setAddress("Calle 123");

        Location loc = new Location(10.0, -74.0);

        when(dealershipPort.findById(1L)).thenReturn(d);
        when(locationPort.geocodeAddress("Calle 123")).thenReturn(loc);

        Dealership result = geocodeDealership.execute(1L);

        assertEquals(10.0, result.getLatitude());
        assertEquals(-74.0, result.getLongitude());
        assertTrue(result.getIsGeolocated());

        verify(dealershipPort).save(argThat(dealership -> dealership.getLatitude() == 10.0 &&
                dealership.getLongitude() == -74.0 &&
                dealership.getIsGeolocated()));
    }

    @Test
    @DisplayName("Debe geocodificar dirección arbitraria")
    /**
     * Verifica geocodificación de una dirección libre sin persistencia.
     */
    void shouldGeocodeArbitraryAddress() {
        Location loc = new Location(5.0, -75.0);
        when(locationPort.geocodeAddress("Some address")).thenReturn(loc);

        Location result = geocodeDealership.geocodeAddress("Some address");

        assertEquals(loc, result);
    }
}
