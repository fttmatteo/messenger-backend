package app.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("Pruebas unitarias de LiveTracking")
class LiveTrackingTest {

    @Test
    @DisplayName("Debe estar inactivo si es antiguo")
    void shouldBeActiveIfRecent() {
        LiveTracking tracking = new LiveTracking();
        tracking.setLastUpdate(LocalDateTime.now().minusMinutes(2));

        assertTrue(tracking.isActive(5));
    }

    @Test
    @DisplayName("Debe retornar la dirección cardinal")
    void shouldBeInactiveIfOld() {
        LiveTracking tracking = new LiveTracking();
        tracking.setLastUpdate(LocalDateTime.now().minusMinutes(10));

        assertFalse(tracking.isActive(5));
    }

    @ParameterizedTest
    @CsvSource({
            "0, Norte",
            "10, Norte",
            "350, Norte",
            "45, Noreste",
            "90, Este",
            "135, Sureste",
            "180, Sur",
            "225, Suroeste",
            "270, Oeste",
            "315, Noroeste"
    })
    @DisplayName("Debe retornar la dirección cardinal")
    void shouldReturnCardinalDirection(double heading, String expectedDirection) {
        LiveTracking tracking = new LiveTracking();
        tracking.setHeading(heading);

        assertEquals(expectedDirection, tracking.getCardinalDirection());
    }

    @Test
    @DisplayName("Debe retornar desconocido para rumbo nulo")

    void shouldReturnUnknownForNullHeading() {
        LiveTracking tracking = new LiveTracking();
        tracking.setHeading(null);

        assertEquals("Desconocido", tracking.getCardinalDirection());
    }
}
