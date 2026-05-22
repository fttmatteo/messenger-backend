package app.infrastructure.external;

import app.domain.events.PlateStatusChangedEvent;
import app.domain.model.WhatsAppSession;
import app.domain.ports.WhatsAppMessagePort;
import app.domain.ports.WhatsAppSessionPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Listener que intercepta cambios de estado en las placas y envía
 * notificaciones proactivas vía WhatsApp.
 */
@Component
public class WhatsAppNotificationListener {

    private static final Logger logger = LoggerFactory.getLogger(WhatsAppNotificationListener.class);

    private final WhatsAppMessagePort messagePort;
    private final WhatsAppSessionPort sessionPort;

    public WhatsAppNotificationListener(WhatsAppMessagePort messagePort, WhatsAppSessionPort sessionPort) {
        this.messagePort = messagePort;
        this.sessionPort = sessionPort;
    }

    @Async("whatsappTaskExecutor")
    @EventListener
    public void handlePlateStatusChanged(PlateStatusChangedEvent event) {
        app.domain.model.ServiceDelivery service = event.getServiceDelivery();
        Long dealershipId = event.getDealershipId();
        
        String plateNumber = (service.getPlate() != null && service.getPlate().getPlateNumber() != null) 
                ? service.getPlate().getPlateNumber() 
                : "Desconocido";
                
        String statusEmoji = event.getNewStatus() != null ? getStatusEmoji(event.getNewStatus()) : "❓";
        String statusName = event.getNewStatus() != null ? getFriendlyStatusName(event.getNewStatus()) : "DESCONOCIDO";

        java.util.List<WhatsAppSession> activeSessions = sessionPort.findActiveSessionsByDealership(dealershipId);

        if (activeSessions.isEmpty()) {
            return;
        }

        logger.info("Enviando notificación de cambio de estado para chasis {} a {} sesiones activas de WhatsApp.",
                app.domain.util.LogSanitizer.maskPlate(plateNumber), activeSessions.size());

        StringBuilder message = new StringBuilder();
        message.append("🔔 *Notificación de cambio de estado*\n\n");
        message.append(String.format("La moto con chasis *%s* ha sido actualizada.\n\n", plateNumber));
        message.append(String.format("*ESTADO:* %s %s\n", statusEmoji, statusName));

        String currentObservation = null;
        if (service.getHistory() != null && !service.getHistory().isEmpty()) {
            currentObservation = service.getHistory().stream()
                    .filter(h -> h.getNewStatus() == event.getNewStatus())
                    .reduce((first, second) -> second)
                    .map(h -> h.getObservation())
                    .orElse(null);
        }
        
        if (currentObservation == null) {
            currentObservation = service.getObservation();
        }

        if (currentObservation != null && !currentObservation.trim().isEmpty()) {
            message.append(String.format("💬 *Observación:* %s", currentObservation.trim()));
        }

        for (WhatsAppSession session : activeSessions) {
            messagePort.sendReplyButtons(
                    session.getPhoneNumber(),
                    message.toString(),
                    java.util.List.of("Ver detalle"),
                    java.util.List.of("VIEW_PLATE_" + plateNumber)
            );
        }
    }

    private String getFriendlyStatusName(app.domain.model.enums.Status status) {
        if (status == null) return "DESCONOCIDO";
        switch (status) {
            case ASSIGNED: return "ASIGNADA";
            case PENDING: return "PENDIENTE";
            case DELIVERED: return "ENTREGADA";
            case RETURNED: return "DEVUELTA";
            case RESOLVED: return "REVISADA";
            case CANCELED: return "CANCELADA";
            case FAILED: return "FALLIDO";
            case DELETED: return "ELIMINADO";
            default: return status.name();
        }
    }

    private String getStatusEmoji(app.domain.model.enums.Status status) {
        if (status == null) return "❓";
        switch (status) {
            case PENDING: return "📝";
            case ASSIGNED: return "⏳";
            case DELIVERED: return "✅";
            case RETURNED: return "↩️";
            case CANCELED: return "❌";
            case RESOLVED: return "✍🏻";
            case FAILED: return "⚠️";
            case DELETED: return "🗑️";
            default: return "❓";
        }
    }
}
