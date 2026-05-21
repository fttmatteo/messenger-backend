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

    private final TrackingPort trackingPort;
    private final EmployeePort employeePort;

    public UpdateLiveTrackingUseCase(TrackingPort trackingPort, EmployeePort employeePort) {
        this.trackingPort = trackingPort;
        this.employeePort = employeePort;
    }

    /**
     * Procesa y guarda una actualización de ubicación en tiempo real.
     * También registra el historial si hay coordenadas válidas con buena precisión.
     */
    public LiveTracking execute(LiveTracking incomingTracking) {

        incomingTracking.setLastUpdate(LocalDateTime.now());

        if (incomingTracking.getStatus() == null) {
            incomingTracking.setStatus(TrackingStatus.ACTIVE);
        }

        enrichMessengerName(incomingTracking);

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

        enrichMessengerName(existing);

        trackingPort.saveLiveLocation(existing);

        return existing;
    }

    /**
     * Enriquece el objeto de tracking con el nombre del mensajero.
     * Primero intenta obtener desde cache (Redis), y si no está,
     * lo obtiene de la base de datos y lo guarda en cache.
     */
    private void enrichMessengerName(LiveTracking tracking) {
        if (tracking.getMessengerName() == null || tracking.getMessengerName().isEmpty()) {
            trackingPort.getMessengerName(tracking.getMessengerId()).ifPresent(tracking::setMessengerName);
        }

        if (tracking.getMessengerName() == null || tracking.getMessengerName().isEmpty()) {
            try {
                Employee employee = employeePort.findById(tracking.getMessengerId());
                if (employee != null && employee.getFullName() != null) {
                    tracking.setMessengerName(employee.getFullName());
                    trackingPort.saveMessengerName(tracking.getMessengerId(), employee.getFullName());
                }
            } catch (Exception e) {
                logger.warn("No se pudo obtener el nombre del mensajero desde la base de datos.");
            }
        }
    }
}
