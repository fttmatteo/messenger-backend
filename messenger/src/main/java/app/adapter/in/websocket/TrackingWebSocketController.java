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
        // Mapeo manual de DTO a Dominio aquí en el controlador (Adaptador)
        LiveTracking domainTracking = new LiveTracking();
        domainTracking.setMessengerId(request.getMessengerId());

        // Crear Location value object
        app.domain.model.Location location = new app.domain.model.Location(
                request.getLatitude(),
                request.getLongitude(),
                java.time.LocalDateTime.now(),
                request.getAccuracy());
        domainTracking.setCurrentLocation(location);

        domainTracking.setSpeed(request.getSpeed());
        domainTracking.setHeading(request.getHeading());
        domainTracking.setDeviceId(request.getDeviceId());
        if (request.getStatus() != null) {
            domainTracking.setStatus(request.getStatus());
        }

        // Procesar y guardar la ubicación
        LiveTracking tracking = updateLiveTracking.execute(domainTracking);

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
