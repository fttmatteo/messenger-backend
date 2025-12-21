package app.application.usecase.tracking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import app.domain.model.LiveTracking;
import app.domain.model.Location;
import app.domain.model.TrackingHistory;
import app.domain.model.enums.TrackingStatus;
import app.domain.ports.TrackingPort;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateLiveTracking Unit Tests")
class UpdateLiveTrackingTest {

    @Mock
    private TrackingPort trackingPort;

    @InjectMocks
    private UpdateLiveTracking updateLiveTracking;

    @Test
    @DisplayName("Debe actualizar rastreo y guardar historial con datos completos")
    void shouldUpdateTrackingWithFullData() {
        LiveTracking tracking = new LiveTracking();
        tracking.setMessengerId(1L);
        tracking.setCurrentLocation(new Location(4.0, -72.0));
        tracking.setSpeed(50.0);
        tracking.setStatus(TrackingStatus.ACTIVE);
        tracking.setLastUpdate(LocalDateTime.now());

        LiveTracking result = updateLiveTracking.execute(tracking);

        assertEquals(tracking, result);
        verify(trackingPort).saveLiveLocation(tracking);
        verify(trackingPort).saveTrackingHistory(any(TrackingHistory.class));
    }

    @Test
    @DisplayName("Debe establecer valores por defecto si faltan")
    void shouldSetDefaultsIfMissing() {
        LiveTracking tracking = new LiveTracking();
        tracking.setMessengerId(1L);
        tracking.setCurrentLocation(new Location(4.0, -72.0));

        LiveTracking result = updateLiveTracking.execute(tracking);

        assertNotNull(result.getLastUpdate());
        assertEquals(TrackingStatus.ACTIVE, result.getStatus());

        verify(trackingPort).saveLiveLocation(tracking);
        verify(trackingPort).saveTrackingHistory(any(TrackingHistory.class));
    }
}
