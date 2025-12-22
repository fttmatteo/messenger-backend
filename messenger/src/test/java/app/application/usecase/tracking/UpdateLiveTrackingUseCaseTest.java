package app.application.usecase.tracking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

import app.domain.model.LiveTracking;
import app.domain.model.Location;
import app.domain.model.TrackingHistory;
import app.domain.model.enums.TrackingStatus;
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

    @InjectMocks
    private UpdateLiveTrackingUseCase useCase;

    @Test
    @DisplayName("Should save live location and history when location provides data")
    void shouldSaveLiveLocationAndHistory() {
        // Given
        LiveTracking incoming = new LiveTracking();
        incoming.setMessengerId(10L);
        incoming.setCurrentLocation(new Location(4.60, -74.08));
        incoming.setSpeed(50.0);

        // When
        LiveTracking result = useCase.execute(incoming);

        // Then
        assertNotNull(result.getLastUpdate());
        assertEquals(TrackingStatus.ACTIVE, result.getStatus());

        // Verify ports
        verify(trackingPort, times(1)).saveLiveLocation(incoming);
        verify(trackingPort, times(1)).saveTrackingHistory(any(TrackingHistory.class));
    }

    @Test
    @DisplayName("Should save only live location if location is missing (keep alive ping)")
    void shouldSaveOnlyLiveLocationIfLocationMissing() {
        // Given
        LiveTracking incoming = new LiveTracking();
        incoming.setMessengerId(10L);
        incoming.setCurrentLocation(null); // No GPS data, just ping

        // When
        useCase.execute(incoming);

        // Then
        verify(trackingPort, times(1)).saveLiveLocation(incoming);
        verify(trackingPort, never()).saveTrackingHistory(any(TrackingHistory.class));
    }
}
