package app.application.usecase.tracking;

import app.domain.model.Employee;
import app.domain.model.LiveTracking;
import app.domain.model.Location;
import app.domain.model.TrackingHistory;
import app.domain.model.enums.TrackingSource;
import app.domain.model.enums.TrackingStatus;
import app.domain.ports.EmployeePort;
import app.domain.ports.TrackingPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

/**
 * Caso de uso para actualizar ubicación en tiempo real de mensajeros.
 */
@Service
public class UpdateLiveTrackingUseCase {

    private static final Logger logger = LoggerFactory.getLogger(UpdateLiveTrackingUseCase.class);

    /**
     * Precisión máxima aceptable en metros para guardar en historial.
     * Ubicaciones con mayor margen de error se ignoran para evitar rutas
     * imprecisas.
     */
    private static final double MAX_ACCEPTABLE_ACCURACY_METERS = 100.0;

    @Autowired
    private TrackingPort trackingPort;

    @Autowired
    private EmployeePort employeePort;

    /**
     * Procesa y guarda una actualización de ubicación en tiempo real.
     * También registra el historial si hay coordenadas válidas con buena precisión.
     */
    public LiveTracking execute(LiveTracking incomingTracking) {

        incomingTracking.setLastUpdate(LocalDateTime.now());

        if (incomingTracking.getStatus() == null) {
            incomingTracking.setStatus(TrackingStatus.ACTIVE);
        }

        if (incomingTracking.getMessengerName() == null || incomingTracking.getMessengerName().isEmpty()) {
            try {
                Employee employee = employeePort.findById(incomingTracking.getMessengerId());
                if (employee != null && employee.getFullName() != null) {
                    incomingTracking.setMessengerName(employee.getFullName());
                }
            } catch (Exception e) {
                logger.warn("No se pudo obtener el nombre del mensajero {}: {}",
                        incomingTracking.getMessengerId(), e.getMessage());
            }
        }

        trackingPort.saveLiveLocation(incomingTracking);
        Location location = incomingTracking.getCurrentLocation();
        if (location != null && location.isValid()) {
            Double accuracy = location.getAccuracy();

            if (accuracy != null && accuracy > MAX_ACCEPTABLE_ACCURACY_METERS) {
            } else {
                TrackingHistory history = new TrackingHistory();
                history.setMessengerId(incomingTracking.getMessengerId());
                history.setLocation(location);
                history.setRecordedAt(incomingTracking.getLastUpdate());
                history.setSource(TrackingSource.GPS);
                history.setSpeed(incomingTracking.getSpeed());

                trackingPort.saveTrackingHistory(history);
            }
        }

        return incomingTracking;
    }

    /**
     * Procesa un heartbeat (señal de vida sin GPS).
     * Solo actualiza lastHeartbeat en Redis, no guarda historial.
     * Útil cuando el mensajero está conectado pero sin señal GPS.
     */
    public LiveTracking executeHeartbeat(LiveTracking heartbeatTracking) {

        LiveTracking existing = trackingPort.getLastLocation(heartbeatTracking.getMessengerId())
                .orElse(new LiveTracking());

        existing.setMessengerId(heartbeatTracking.getMessengerId());
        existing.setLastHeartbeat(heartbeatTracking.getLastHeartbeat() != null
                ? heartbeatTracking.getLastHeartbeat()
                : LocalDateTime.now());
        existing.setStatus(TrackingStatus.ACTIVE);

        if (existing.getMessengerName() == null || existing.getMessengerName().isEmpty()) {
            try {
                Employee employee = employeePort.findById(heartbeatTracking.getMessengerId());
                if (employee != null && employee.getFullName() != null) {
                    existing.setMessengerName(employee.getFullName());
                }
            } catch (Exception e) {
                logger.warn("No se pudo obtener el nombre del mensajero {}: {}",
                        heartbeatTracking.getMessengerId(), e.getMessage());
            }
        }

        trackingPort.saveLiveLocation(existing);

        return existing;
    }
}
