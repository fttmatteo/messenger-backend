package app.adapter.in.rest.mapper;

import app.adapter.in.rest.response.LiveTrackingResponse;
import app.adapter.in.rest.response.TrackingHistoryResponse;
import app.domain.model.LiveTracking;
import app.domain.model.TrackingHistory;
import org.springframework.stereotype.Component;

/**
 * Mapper de LiveTracking y TrackingHistory a respuestas REST.
 */
@Component
public class TrackingResponseMapper {

    public LiveTrackingResponse toResponse(LiveTracking tracking) {
        if (tracking == null) {
            return null;
        }
        return new LiveTrackingResponse(
                tracking.getMessengerId(),
                tracking.getMessengerName(),
                tracking.getCurrentLocation() != null ? tracking.getCurrentLocation().getLatitude() : null,
                tracking.getCurrentLocation() != null ? tracking.getCurrentLocation().getLongitude() : null,
                tracking.getLastUpdate(),
                tracking.getLastHeartbeat(),
                tracking.getStatus(),
                tracking.getSpeed(),
                tracking.getHeading());
    }

    public TrackingHistoryResponse toHistoryResponse(TrackingHistory history) {
        if (history == null) {
            return null;
        }
        TrackingHistoryResponse response = new TrackingHistoryResponse();
        response.setHistoryId(history.getHistoryId());
        response.setMessengerId(history.getMessengerId());
        response.setLatitude(history.getLocation() != null ? history.getLocation().getLatitude() : null);
        response.setLongitude(history.getLocation() != null ? history.getLocation().getLongitude() : null);
        response.setRecordedAt(history.getRecordedAt());
        response.setServiceDeliveryId(history.getServiceDeliveryId());
        response.setSource(history.getSource() != null ? history.getSource().name() : null);
        response.setSpeed(history.getSpeed());
        return response;
    }
}
