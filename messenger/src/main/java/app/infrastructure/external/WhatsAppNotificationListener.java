package app.infrastructure.external;

import app.domain.events.PlateStatusChangedEvent;
import app.domain.model.WhatsAppSession;
import app.domain.ports.WhatsAppMessagePort;
import app.domain.ports.WhatsAppSessionPort;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Listener que intercepta cambios de estado en las placas y envía
 * notificaciones proactivas vía WhatsApp.
 */
@Component
public class WhatsAppNotificationListener {

    private final WhatsAppMessagePort messagePort;
    private final WhatsAppSessionPort sessionPort;

    public WhatsAppNotificationListener(WhatsAppMessagePort messagePort, WhatsAppSessionPort sessionPort) {
        this.messagePort = messagePort;
        this.sessionPort = sessionPort;
    }

    @Async("whatsappTaskExecutor")
    @EventListener
    public void handlePlateStatusChanged(PlateStatusChangedEvent event) {
        Long dealershipId = event.getDealershipId();
        String plateNumber = event.getPlateNumber();
        String dealershipName = event.getDealershipName();
        String newStatusName = getFriendlyStatusName(event.getNewStatus());

        java.util.List<WhatsAppSession> activeSessions = sessionPort.findActiveSessionsByDealership(dealershipId);

        if (activeSessions.isEmpty()) {
            return;
        }

        String message = String.format(
                "🔔 *Notificación de cambio de estado*\n\n" +
                        "La moto con chasis *%s* para el concesionario *%s* ha cambiado su estado a:\n\n" +
                        "➡️ *%s*\n\n" +
                        "_Consulte el menú para más detalles._",
                plateNumber, dealershipName, newStatusName);

        for (WhatsAppSession session : activeSessions) {
            messagePort.sendTextMessage(session.getPhoneNumber(), message);
        }
    }

    private String getFriendlyStatusName(app.domain.model.enums.Status status) {
        switch (status) {
            case ASSIGNED:
                return "ASIGNADA";
            case PENDING:
                return "PENDIENTE";
            case DELIVERED:
                return "ENTREGADA";
            case RETURNED:
                return "DEVUELTA";
            case RESOLVED:
                return "REVISADA";
            case CANCELED:
                return "CANCELADA";
            default:
                return status.name();
        }
    }
}
