package app.adapter.in.websocket;

import app.adapter.in.rest.request.LiveTrackingRequest;
import app.adapter.in.rest.response.LiveTrackingResponse;
import app.application.usecase.tracking.UpdateLiveTracking;
import app.domain.model.LiveTracking;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

/**
 * Controlador WebSocket para tracking en tiempo real.
 * 
 * Flujo:
 * 1. Mensajero conecta a /ws/tracking
 * 2. Mensajero envía ubicación a /app/tracking/update
 * 3. Servidor procesa y guarda la ubicación
 * 4. Servidor hace broadcast a /topic/tracking/{messengerId}
 * 5. Admins suscritos reciben la actualización
 */
@Controller
public class TrackingWebSocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private UpdateLiveTracking updateLiveTracking;

    /**
     * Recibe actualizaciones de ubicación de los mensajeros.
     * 
     * @param request   Datos de ubicación del mensajero
     * @param principal Usuario autenticado (mensajero)
     */
    @MessageMapping("/tracking/update")
    public void receiveLocationUpdate(LiveTrackingRequest request, Principal principal) {
        // Procesar y guardar la ubicación
        LiveTracking tracking = updateLiveTracking.execute(request);

        // Broadcast a admins suscritos a este mensajero específico
        LiveTrackingResponse response = mapToResponse(tracking);
        messagingTemplate.convertAndSend(
                "/topic/tracking/" + tracking.getMessengerId(),
                response);

        // Broadcast a todos los admins suscritos al canal general
        messagingTemplate.convertAndSend("/topic/tracking/all", response);
    }

    /**
     * Endpoint para que los admins se suscriban a actualizaciones de todos los
     * mensajeros.
     */
    @MessageMapping("/tracking/subscribe/all")
    @SendTo("/topic/tracking/all")
    public String subscribeToAll() {
        return "Suscrito a actualizaciones de todos los mensajeros";
    }

    private LiveTrackingResponse mapToResponse(LiveTracking tracking) {
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
}
