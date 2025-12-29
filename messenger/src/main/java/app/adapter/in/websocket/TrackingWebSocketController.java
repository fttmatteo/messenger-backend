package app.adapter.in.websocket;

import app.adapter.in.rest.request.LiveTrackingRequest;
import app.adapter.in.rest.response.LiveTrackingResponse;
import app.application.usecase.tracking.UpdateLiveTrackingUseCase;
import app.domain.model.LiveTracking;
import app.domain.model.Location;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import java.security.Principal;
import java.time.LocalDateTime;

/**
 * Controlador WebSocket para tracking en tiempo real de mensajeros.
 */
@Controller
public class TrackingWebSocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private UpdateLiveTrackingUseCase updateLiveTracking;

    /**
     * Recibe actualizaciones de ubicación en tiempo real de los mensajeros.
     * Procesa los datos y los retransmite a los suscriptores.
     */
    @MessageMapping("/tracking/update")
    public void receiveLocationUpdate(LiveTrackingRequest request, Principal principal) {
        LiveTracking domainTracking = new LiveTracking();
        domainTracking.setMessengerId(request.getMessengerId());

        if (request.getLatitude() != null && request.getLongitude() != null) {
            Location location = new Location(
                    request.getLatitude(),
                    request.getLongitude(),
                    LocalDateTime.now(), // Hora sincronizada del servidor
                    request.getAccuracy());
            domainTracking.setCurrentLocation(location);
        }

        domainTracking.setSpeed(request.getSpeed());
        domainTracking.setHeading(request.getHeading());
        domainTracking.setDeviceId(request.getDeviceId());
        if (request.getStatus() != null) {
            domainTracking.setStatus(request.getStatus());
        }
        domainTracking.setLastUpdate(request.getLastUpdate());

        LiveTracking tracking = updateLiveTracking.execute(domainTracking);
        LiveTrackingResponse response = mapToResponse(tracking);
        messagingTemplate.convertAndSend(
                "/topic/tracking/" + tracking.getMessengerId(),
                response);

        messagingTemplate.convertAndSend("/topic/tracking/all", response);
    }

    /**
     * Permite a los clientes (admin) suscribirse a todas las actualizaciones de
     * tracking.
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
