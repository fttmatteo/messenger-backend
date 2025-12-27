package app.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DealershipTest {

    @Test
    @DisplayName("Should return null Location when lat/lon are missing")
    /**
     * Verifica que getLocation devuelva null si falta latitud o longitud.
     */
    void shouldReturnNullLocationIfMissing() {
        Dealership dealership = new Dealership();
        dealership.setLatitude(null);
        dealership.setLongitude(null);

        assertNull(dealership.getLocation());
    }

    @Test
    @DisplayName("Should return correct Location when lat/lon are present")
    /**
     * Verifica que getLocation devuelva un objeto Location correcto si hay
     * coordenadas.
     */
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
    @DisplayName("Should set lat/lon/isGeolocated from Location")
    /**
     * Verifica que setLocation actualice latitud, longitud y flag de
     * geolocalización.
     */
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
