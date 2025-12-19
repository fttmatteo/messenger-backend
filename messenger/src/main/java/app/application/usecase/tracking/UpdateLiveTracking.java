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
 * Caso de uso para procesar y almacenar actualizaciones de ubicación en tiempo
 * real.
 * 
 * Gestiona el flujo de datos de rastreo entrantes, asegurando que se almacenen
 * tanto en la caché de tiempo real (para monitoreo en vivo) como en el
 * historial
 * persistente (para auditoría futura).
 */
@Service
public class UpdateLiveTracking {

    @Autowired
    private TrackingPort trackingPort;

    /**
     * Ejecuta la actualización de rastreo para un mensajero.
     * 
     * Normaliza los datos (establece fecha y estado por defecto si faltan),
     * actualiza la ubicación en tiempo real y guarda un registro en el historial
     * permanente.
     * 
     * @param incomingTracking Objeto con los datos de ubicación recibidos del
     *                         dispositivo.
     * @return El objeto LiveTracking procesado y enriquecido con datos por defecto.
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

        // Guardar en BD (historial) solo si hay una ubicación válida
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
