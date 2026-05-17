package app.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Pruebas unitarias de Dealership")
class DealershipTest {

    @Test
    @DisplayName("Debe retornar ubicación nula si falta")
    void shouldReturnNullLocationIfMissing() {
        Dealership dealership = new Dealership();
        dealership.setLatitude(null);
        dealership.setLongitude(null);

        assertNull(dealership.getLocation());
    }

    @Test
    @DisplayName("Debe retornar ubicación si está presente")

    void shouldReturnLocationIfPresent() {
        Dealership dealership = new Dealership();
        dealership.setLatitude(4.5);
        dealership.setLongitude(-74.5);

        Location location = dealership.getLocation();
        assertNotNull(location);
        assertEquals(4.5, location.getLatitude());
        assertEquals(-74.5, location.getLongitude());
    }

    @Test
    @DisplayName("Debe establecer la ubicación")

    void shouldSetLocation() {
        Dealership dealership = new Dealership();
        assertFalse(dealership.getIsGeolocated());

        Location location = new Location(10.0, -20.0);
        dealership.setLocation(location);

        assertEquals(10.0, dealership.getLatitude());
        assertEquals(-20.0, dealership.getLongitude());
        assertTrue(dealership.getIsGeolocated());
    }
}
