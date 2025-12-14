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
     * @param incomingTracking Datos de ubicación recibidos (modelo de dominio)
     * @return LiveTracking con los datos procesados
     */
    public LiveTracking execute(LiveTracking incomingTracking) {
        // Asegurar que la fecha de actualización es la actual si no viene seteada
        if (incomingTracking.getLastUpdate() == null) {
            incomingTracking.setLastUpdate(LocalDateTime.now());
        }

        // Si el estado es nulo, por defecto ACTIVE
        if (incomingTracking.getStatus() == null) {
            incomingTracking.setStatus(TrackingStatus.ACTIVE);
        }

        // Guardar en Redis (ubicación en tiempo real)
        trackingPort.saveLiveLocation(incomingTracking);

        // Guardar en BD (historial)
        TrackingHistory history = new TrackingHistory();
        history.setMessengerId(incomingTracking.getMessengerId());
        history.setLocation(incomingTracking.getCurrentLocation());
        history.setRecordedAt(incomingTracking.getLastUpdate());
        history.setSource(TrackingSource.GPS); // Asumimos GPS por defecto para live tracking
        history.setSpeed(incomingTracking.getSpeed());

        trackingPort.saveTrackingHistory(history);

        return incomingTracking;
    }
}
