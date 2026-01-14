package app.application.usecase.tracking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import java.util.Optional;
import app.domain.model.LiveTracking;
import app.domain.model.Location;
import app.domain.model.TrackingHistory;
import app.domain.model.enums.TrackingStatus;
import app.domain.ports.EmployeePort;
import app.domain.ports.TrackingPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateLiveTrackingUseCaseTest {

    @Mock
    private TrackingPort trackingPort;

    @Mock
    private EmployeePort employeePort;

    @InjectMocks
    private UpdateLiveTrackingUseCase useCase;

    @Test
    @DisplayName("Should save live location and history when location has good accuracy")
    void shouldSaveLiveLocationAndHistoryWithGoodAccuracy() {
        LiveTracking incoming = new LiveTracking();
        incoming.setMessengerId(10L);
        incoming.setCurrentLocation(new Location(4.60, -74.08, null, 50.0));
        incoming.setSpeed(50.0);

        LiveTracking result = useCase.execute(incoming);
        assertNotNull(result.getLastUpdate());
        assertEquals(TrackingStatus.ACTIVE, result.getStatus());

        verify(trackingPort, times(1)).saveLiveLocation(incoming);
        verify(trackingPort, times(1)).saveTrackingHistory(any(TrackingHistory.class));
    }

    @Test
    @DisplayName("Should save only live location if accuracy is too low (> 100m)")
    void shouldNotSaveHistoryIfAccuracyTooLow() {
        LiveTracking incoming = new LiveTracking();
        incoming.setMessengerId(10L);
        incoming.setCurrentLocation(new Location(4.60, -74.08, null, 150.0));
        incoming.setSpeed(50.0);
        useCase.execute(incoming);
        verify(trackingPort, times(1)).saveLiveLocation(incoming);
        verify(trackingPort, never()).saveTrackingHistory(any(TrackingHistory.class));
    }

    @Test
    @DisplayName("Should save only live location if location is missing (keep alive ping)")
    void shouldSaveOnlyLiveLocationIfLocationMissing() {
        LiveTracking incoming = new LiveTracking();
        incoming.setMessengerId(10L);
        incoming.setCurrentLocation(null);

        useCase.execute(incoming);

        verify(trackingPort, times(1)).saveLiveLocation(incoming);
        verify(trackingPort, never()).saveTrackingHistory(any(TrackingHistory.class));
    }

    @Test
    @DisplayName("Should update only heartbeat when executeHeartbeat is called")
    void shouldUpdateOnlyHeartbeatWithExecuteHeartbeat() {
        LiveTracking heartbeat = new LiveTracking();
        heartbeat.setMessengerId(10L);

        when(trackingPort.getLastLocation(10L)).thenReturn(Optional.empty());

        LiveTracking result = useCase.executeHeartbeat(heartbeat);

        assertNotNull(result.getLastHeartbeat());
        assertEquals(TrackingStatus.ACTIVE, result.getStatus());

        verify(trackingPort, times(1)).saveLiveLocation(any(LiveTracking.class));
        verify(trackingPort, never()).saveTrackingHistory(any(TrackingHistory.class));
    }

    @Test
    @DisplayName("Should preserve existing location data when heartbeat is received")
    void shouldPreserveExistingLocationOnHeartbeat() {
        LiveTracking existing = new LiveTracking();
        existing.setMessengerId(10L);
        existing.setCurrentLocation(new Location(4.60, -74.08));
        existing.setMessengerName("Test User");

        LiveTracking heartbeat = new LiveTracking();
        heartbeat.setMessengerId(10L);

        when(trackingPort.getLastLocation(10L)).thenReturn(Optional.of(existing));

        LiveTracking result = useCase.executeHeartbeat(heartbeat);

        assertNotNull(result.getCurrentLocation());
        assertEquals("Test User", result.getMessengerName());
        assertNotNull(result.getLastHeartbeat());
    }
}
