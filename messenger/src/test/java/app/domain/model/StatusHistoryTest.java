package app.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("StatusHistory Unit Tests")
class StatusHistoryTest {

    @Test
    @DisplayName("Debe convertir Lat/Lng a Location correctamente")
    /**
     * Verifica la conversión de coordenadas planas a objeto Location.
     */
    void shouldConvertWhenLatLongArePresent() {
        StatusHistory history = new StatusHistory();
        history.setDeliveryLatitude(4.0);
        history.setDeliveryLongitude(-72.0);

        Location location = history.getDeliveryLocation();

        assertEquals(4.0, location.getLatitude());
        assertEquals(-72.0, location.getLongitude());
    }

    @Test
    @DisplayName("Debe retornar null cuando falla Lat o Lng")
    /**
     * Verifica que se retorne null cuando faltan coordenadas.
     */
    void shouldReturnNullWhenCoordinatesMissing() {
        StatusHistory history = new StatusHistory();

        assertNull(history.getDeliveryLocation());

        history.setDeliveryLatitude(4.0);
        assertNull(history.getDeliveryLocation());
    }

    @Test
    @DisplayName("Debe establecer Lat/Lng desde Location")
    /**
     * Verifica que se establezcan las coordenadas cuando se proporciona un objeto Location.
     */
    void shouldSetFromLocation() {
        StatusHistory history = new StatusHistory();
        Location loc = new Location(1.0, 2.0);

        history.setDeliveryLocation(loc);

        assertEquals(1.0, history.getDeliveryLatitude());
        assertEquals(2.0, history.getDeliveryLongitude());
    }

    @Test
    @DisplayName("No debe establecer Lat/Lng si Location es null")
    /**
     * Verifica que no se establezcan las coordenadas cuando se proporciona un objeto Location nulo.
     */
    void shouldNotSetIfLocationNull() {
        StatusHistory history = new StatusHistory();
        history.setDeliveryLatitude(5.0);

        history.setDeliveryLocation(null);

        assertEquals(5.0, history.getDeliveryLatitude());
    }
}
