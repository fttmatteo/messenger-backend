package app.application.usecase.tracking;

import app.domain.model.Employee;
import app.domain.model.LiveTracking;
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

    @Autowired
    private TrackingPort trackingPort;

    @Autowired
    private EmployeePort employeePort;

    /**
     * Procesa y guarda una actualización de ubicación en tiempo real.
     * También registra el historial si hay coordenadas válidas.
     */
    public LiveTracking execute(LiveTracking incomingTracking) {
        logger.debug("Actualizando ubicación para mensajero ID: {}", incomingTracking.getMessengerId());

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

        if (incomingTracking.getCurrentLocation() != null) {
            TrackingHistory history = new TrackingHistory();
            history.setMessengerId(incomingTracking.getMessengerId());
            history.setLocation(incomingTracking.getCurrentLocation());
            history.setRecordedAt(incomingTracking.getLastUpdate());
            history.setSource(TrackingSource.GPS);
            history.setSpeed(incomingTracking.getSpeed());

            trackingPort.saveTrackingHistory(history);
        }

        return incomingTracking;
    }
}
