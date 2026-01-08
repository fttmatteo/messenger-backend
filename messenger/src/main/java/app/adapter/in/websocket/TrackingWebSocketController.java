package app.adapter.in.websocket;

import app.adapter.in.rest.request.LiveTrackingRequest;
import app.adapter.in.rest.response.LiveTrackingResponse;
import app.application.usecase.tracking.UpdateLiveTrackingUseCase;
import app.domain.model.LiveTracking;
import app.domain.model.Location;
import app.domain.model.enums.TrackingStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Controlador WebSocket para tracking en tiempo real de mensajeros.
 */
@Controller
public class TrackingWebSocketController {

    private static final Logger logger = LoggerFactory.getLogger(TrackingWebSocketController.class);

    /**
     * Intervalo mínimo en milisegundos entre updates de un mismo mensajero.
     * Evita flood de requests y reduce carga en base de datos.
     */
    private static final long MIN_UPDATE_INTERVAL_MS = 5000; // 5 segundos

    /**
     * Cache de últimos timestamps de update por mensajero.
     */
    private final ConcurrentHashMap<Long, Long> lastUpdateTimestamps = new ConcurrentHashMap<>();

    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private UpdateLiveTrackingUseCase updateLiveTracking;

    /**
     * Recibe actualizaciones de ubicación en tiempo real de los mensajeros.
     * Procesa los datos y los retransmite a los suscriptores.
     * Incluye rate limiting para evitar flood de requests.
     */
    @MessageMapping("/tracking/update")
    public void receiveLocationUpdate(LiveTrackingRequest request, Principal principal) {
        Long messengerId = request.getMessengerId();

        logger.debug("📍 Ubicación recibida del mensajero {}: lat={}, lng={}, accuracy={}m, status={}",
                messengerId,
                request.getLatitude(),
                request.getLongitude(),
                request.getAccuracy(),
                request.getStatus());

        // Permitir siempre cambios de status (OFFLINE/INACTIVE importantes)
        boolean isStatusChange = request.getStatus() != null &&
                request.getStatus() != TrackingStatus.ACTIVE;

        // Rate limiting: ignorar updates demasiado frecuentes (excepto cambios de
        // status)
        if (!isStatusChange && !shouldProcessUpdate(messengerId)) {
            logger.debug("Rate limited: update ignorado para mensajero {} (muy frecuente)", messengerId);
            return;
        }

        // Validar que las coordenadas sean válidas
        if (request.getLatitude() != null && request.getLongitude() != null) {
            if (request.getLatitude() == 0 && request.getLongitude() == 0) {
                logger.warn("⚠️ Coordenadas inválidas (0,0) recibidas del mensajero {}", messengerId);
            } else {
                logger.info("✅ Ubicación válida recibida del mensajero {}: lat={}, lng={}, accuracy={}m",
                        messengerId, request.getLatitude(), request.getLongitude(), request.getAccuracy());
            }
        } else {
            logger.debug("📡 Heartbeat/Update sin coordenadas del mensajero {}", messengerId);
        }

        LiveTracking domainTracking = new LiveTracking();
        domainTracking.setMessengerId(messengerId);

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

        LiveTracking tracking = updateLiveTracking.execute(domainTracking);
        LiveTrackingResponse response = mapToResponse(tracking);
        messagingTemplate.convertAndSend(
                "/topic/tracking/" + tracking.getMessengerId(),
                response);

        messagingTemplate.convertAndSend("/topic/tracking/all", response);
    }

    /**
     * Verifica si debe procesar el update basándose en rate limiting.
     * Actualiza el timestamp si se permite el procesamiento.
     */
    private boolean shouldProcessUpdate(Long messengerId) {
        if (messengerId == null) {
            return false;
        }

        long now = System.currentTimeMillis();
        Long lastUpdate = lastUpdateTimestamps.get(messengerId);

        if (lastUpdate == null || (now - lastUpdate) >= MIN_UPDATE_INTERVAL_MS) {
            lastUpdateTimestamps.put(messengerId, now);
            return true;
        }

        return false;
    }

    /**
     * Recibe heartbeats de mensajeros (señal de vida sin GPS).
     * Permite saber que el mensajero está conectado aunque no tenga GPS.
     */
    @MessageMapping("/tracking/heartbeat")
    public void receiveHeartbeat(LiveTrackingRequest request, Principal principal) {
        Long messengerId = request.getMessengerId();
        if (messengerId == null) {
            return;
        }

        logger.debug("Heartbeat recibido de mensajero {}", messengerId);

        // Crear tracking mínimo solo con heartbeat
        LiveTracking domainTracking = new LiveTracking();
        domainTracking.setMessengerId(messengerId);
        domainTracking.setLastHeartbeat(LocalDateTime.now());
        domainTracking.setStatus(TrackingStatus.ACTIVE);

        // El UseCase actualizará solo el heartbeat en Redis, no guardará historial
        LiveTracking tracking = updateLiveTracking.executeHeartbeat(domainTracking);
        LiveTrackingResponse response = mapToResponse(tracking);

        // Notificar a admins
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
                tracking.getLastHeartbeat(),
                tracking.getStatus(),
                tracking.getSpeed(),
                tracking.getHeading());
    }
}
