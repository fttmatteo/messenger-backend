package app.adapter.in.rest.mapper;

import app.adapter.in.rest.response.LiveTrackingResponse;
import app.adapter.in.rest.response.TrackingHistoryResponse;
import app.domain.model.LiveTracking;
import app.domain.model.TrackingHistory;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entidades de tracking a DTOs de respuesta.
 *
 * Facilita la transformación de objetos de dominio {@link LiveTracking} y
 * {@link TrackingHistory}
 * a sus correspondientes respuestas REST, {@link LiveTrackingResponse} y
 * {@link TrackingHistoryResponse}.
 */
@Component
public class TrackingResponseMapper {

    /**
     * Convierte un objeto LiveTracking a LiveTrackingResponse.
     *
     * @param tracking Objeto de dominio con la información de rastreo en vivo.
     * @return DTO LiveTrackingResponse.
     */
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
                tracking.getStatus(),
                tracking.getSpeed(),
                tracking.getHeading());
    }

    /**
     * Convierte un objeto TrackingHistory a TrackingHistoryResponse.
     *
     * @param history Objeto de dominio con el historial de rastreo.
     * @return DTO TrackingHistoryResponse.
     */
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
