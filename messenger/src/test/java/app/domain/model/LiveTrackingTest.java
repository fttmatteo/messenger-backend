package app.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class LiveTrackingTest {

    @Test
    @DisplayName("Should be active if updated recently")
    void shouldBeActiveIfRecent() {
        LiveTracking tracking = new LiveTracking();
        tracking.setLastUpdate(LocalDateTime.now().minusMinutes(2));

        assertTrue(tracking.isActive(5));
    }

    @Test
    @DisplayName("Should be inactive if updated long ago")
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
    @DisplayName("Should return correct cardinal direction")
    void shouldReturnCardinalDirection(double heading, String expectedDirection) {
        LiveTracking tracking = new LiveTracking();
        tracking.setHeading(heading);

        assertEquals(expectedDirection, tracking.getCardinalDirection());
    }

    @Test
    @DisplayName("Should return 'Desconocido' for null heading")
    void shouldReturnUnknownForNullHeading() {
        LiveTracking tracking = new LiveTracking();
        tracking.setHeading(null);

        assertEquals("Desconocido", tracking.getCardinalDirection());
    }
}
