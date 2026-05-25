package app.adapter.out.persistence.listener;

import app.domain.events.PlateStatusChangedEvent;
import app.domain.model.ServiceDelivery;
import app.domain.model.StatusHistory;
import app.domain.model.TimelineEvent;
import app.domain.ports.TimelineEventPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Comparator;

/**
 * Proyector encargado de actualizar el modelo de lectura (CQRS)
 * para la Línea de Tiempo de Monitoreo.
 */
@Component
public class TimelineEventProjector {

    private static final Logger logger = LoggerFactory.getLogger(TimelineEventProjector.class);

    private final TimelineEventPort timelineEventPort;

    public TimelineEventProjector(TimelineEventPort timelineEventPort) {
        this.timelineEventPort = timelineEventPort;
    }

    /**
     * Escucha el evento de cambio de estado y actualiza el Read Model de manera asíncrona.
     * Se usa TransactionalEventListener para asegurar que el evento solo se procese
     * si la transacción de base de datos del servicio principal fue exitosa.
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPlateStatusChanged(PlateStatusChangedEvent event) {
        try {
            ServiceDelivery service = event.getServiceDelivery();
            
            if (service == null || service.getMessenger() == null) {
                return;
            }

            StatusHistory latestHistory = null;
            if (service.getHistory() != null && !service.getHistory().isEmpty()) {
                latestHistory = service.getHistory().stream()
                        .max(Comparator.comparing(StatusHistory::getChangeDate))
                        .orElse(null);
            }

            TimelineEvent timelineEvent = new TimelineEvent();
            timelineEvent.setMessengerId(service.getMessenger().getIdEmployee());
            
            if (latestHistory != null && latestHistory.getChangeDate() != null) {
                timelineEvent.setEventDate(latestHistory.getChangeDate().toLocalDate());
                timelineEvent.setTimestamp(latestHistory.getChangeDate());
                timelineEvent.setLatitude(latestHistory.getDeliveryLatitude());
                timelineEvent.setLongitude(latestHistory.getDeliveryLongitude());
                
                if (latestHistory.getChangedBy() != null) {
                    timelineEvent.setChangedByName(latestHistory.getChangedBy().getFullName());
                    if (latestHistory.getChangedBy().getRole() != null) {
                        timelineEvent.setChangedByRole(latestHistory.getChangedBy().getRole().name());
                    }
                }
            } else {
                java.time.LocalDateTime now = java.time.LocalDateTime.now();
                timelineEvent.setEventDate(now.toLocalDate());
                timelineEvent.setTimestamp(now);
            }

            timelineEvent.setStatus(event.getNewStatus());
            timelineEvent.setPlateNumber(event.getPlateNumber());
            timelineEvent.setDealershipName(event.getDealershipName());

            timelineEventPort.save(timelineEvent);
            logger.info("Proyección de TimelineEvent guardada exitosamente.");

        } catch (Exception e) {
            logger.error("Error al proyectar el evento de línea de tiempo: {}", e.getMessage(), e);
        }
    }
}
