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
 * Controlador WebSocket para rastreo de mensajeros en tiempo real.
 * 
 * Este controlador maneja la comunicación bidireccional en tiempo real entre
 * los mensajeros (que envían su ubicación) y los administradores (que
 * monitorean
 * las ubicaciones). Utiliza el protocolo STOMP sobre WebSocket.
 * 
 * Arquitectura de comunicación:
 * 
 * Flujo de actualización de ubicación:
 * 1. Mensajero se conecta al endpoint /ws/tracking
 * 2. Mensajero envía su ubicación a /app/tracking/update
 * 3. Servidor procesa, valida y persiste la ubicación
 * 4. Servidor hace broadcast a dos canales:
 * - /topic/tracking/{messengerId} (para seguimiento específico)
 * - /topic/tracking/all (para vista general de todos los mensajeros)
 * 5. Administradores suscritos reciben la actualización en tiempo real
 * 
 * Endpoints disponibles:
 * - /app/tracking/update: Recibe actualizaciones de ubicación de mensajeros
 * - /app/tracking/subscribe/all: Suscripción a todas las actualizaciones
 * 
 * Canales de broadcast:
 * - /topic/tracking/{messengerId}: Actualizaciones de un mensajero específico
 * - /topic/tracking/all: Actualizaciones de todos los mensajeros
 * 
 * @see app.adapter.in.websocket.config.WebSocketConfig
 * @see app.application.usecase.tracking.UpdateLiveTracking
 * @see app.adapter.in.rest.request.LiveTrackingRequest
 * @see app.adapter.in.rest.response.LiveTrackingResponse
 */
@Controller
public class TrackingWebSocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private UpdateLiveTracking updateLiveTracking;

    /**
     * Recibe y procesa actualizaciones de ubicación de los mensajeros en tiempo
     * real.
     * 
     * Este método es invocado cuando un mensajero envía su ubicación actual.
     * Realiza las siguientes operaciones:
     * 1. Mapea el DTO de entrada al modelo de dominio
     * 2. Ejecuta el caso de uso para actualizar y persistir la ubicación
     * 3. Hace broadcast de la actualización a dos canales:
     * - Canal específico del mensajero (/topic/tracking/{messengerId})
     * - Canal general de todos los mensajeros (/topic/tracking/all)
     * 
     * Los administradores suscritos a estos canales recibirán la actualización
     * inmediatamente para visualización en tiempo real en mapas.
     * 
     * @param request   Datos de ubicación del mensajero (latitud, longitud,
     *                  velocidad, etc.)
     * @param principal Usuario autenticado (mensajero que envía la ubicación)
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
     * Endpoint de suscripción para que los administradores reciban actualizaciones
     * de todos los mensajeros.
     * 
     * Los clientes que se conecten a este endpoint serán automáticamente suscritos
     * al canal /topic/tracking/all y recibirán todas las actualizaciones de
     * ubicación
     * de todos los mensajeros activos en el sistema.
     * 
     * Este endpoint es útil para dashboards de administración que necesitan
     * mostrar la ubicación de todos los mensajeros simultáneamente en un mapa.
     * 
     * @return Mensaje de confirmación de suscripción
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
