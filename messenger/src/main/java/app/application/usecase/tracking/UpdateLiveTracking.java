package app.application.usecase.tracking;

import app.domain.model.LiveTracking;
import app.domain.model.TrackingHistory;
import app.domain.model.enums.TrackingSource;
import app.domain.model.enums.TrackingStatus;
import app.domain.ports.TrackingPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

/**
 * Caso de uso para actualizar ubicación en tiempo real de mensajeros.
 */
@Service
public class UpdateLiveTracking {

    @Autowired
    private TrackingPort trackingPort;

    public LiveTracking execute(LiveTracking incomingTracking) {
        if (incomingTracking.getLastUpdate() == null) {
            incomingTracking.setLastUpdate(LocalDateTime.now());
        }

        if (incomingTracking.getStatus() == null) {
            incomingTracking.setStatus(TrackingStatus.ACTIVE);
        }

        trackingPort.saveLiveLocation(incomingTracking);

        if (incomingTracking.getCurrentLocation() != null) {
            TrackingHistory history = new TrackingHistory();
            history.setMessengerId(incomingTracking.getMessengerId());
            history.setLocation(incomingTracking.getCurrentLocation());
            history.setRecordedAt(incomingTracking.getLastUpdate());
            history.setSource(TrackingSource.GPS); // Asumimos GPS por defecto para live tracking
            history.setSpeed(incomingTracking.getSpeed());

            trackingPort.saveTrackingHistory(history);
        }

        return incomingTracking;
    }
}
