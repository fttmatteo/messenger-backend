package app.application.usecase.tracking;

import app.adapter.in.rest.request.LiveTrackingRequest;
import app.domain.model.LiveTracking;
import app.domain.model.Location;
import app.domain.model.TrackingHistory;
import app.domain.model.enums.TrackingSource;
import app.domain.model.enums.TrackingStatus;
import app.domain.ports.TrackingPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Caso de uso para actualizar la ubicación en tiempo real de un mensajero.
 * Guarda en Redis para tracking en vivo y en la BD para historial.
 */
@Service
public class UpdateLiveTracking {

    @Autowired
    private TrackingPort trackingPort;

    /**
     * Procesa una actualización de ubicación del mensajero.
     * 
     * @param request Datos de ubicación recibidos
     * @return LiveTracking con los datos procesados
     */
    public LiveTracking execute(LiveTrackingRequest request) {
        // Crear objeto Location
        Location location = new Location(
                request.getLatitude(),
                request.getLongitude(),
                LocalDateTime.now(),
                request.getAccuracy());

        // Crear LiveTracking para Redis
        LiveTracking liveTracking = new LiveTracking();
        liveTracking.setMessengerId(request.getMessengerId());
        liveTracking.setCurrentLocation(location);
        liveTracking.setLastUpdate(LocalDateTime.now());
        liveTracking.setStatus(request.getStatus() != null ? request.getStatus() : TrackingStatus.ACTIVE);
        liveTracking.setSpeed(request.getSpeed());
        liveTracking.setHeading(request.getHeading());
        liveTracking.setDeviceId(request.getDeviceId());

        // Guardar en Redis (ubicación en tiempo real)
        trackingPort.saveLiveLocation(liveTracking);

        // Guardar en BD (historial)
        TrackingHistory history = new TrackingHistory();
        history.setMessengerId(request.getMessengerId());
        history.setLocation(location);
        history.setRecordedAt(LocalDateTime.now());
        history.setSource(TrackingSource.GPS);
        history.setSpeed(request.getSpeed());

        trackingPort.saveTrackingHistory(history);

        return liveTracking;
    }
}
