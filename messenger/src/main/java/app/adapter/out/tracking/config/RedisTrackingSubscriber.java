package app.adapter.out.tracking.config;

import app.adapter.in.rest.tracking.LiveTrackingResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * Suscriptor de Redis que escucha actualizaciones de tracking y las
 * retransmite a los clientes WebSocket conectados a esta instancia.
 */
@Component
public class RedisTrackingSubscriber {

    private static final Logger logger = LoggerFactory.getLogger(RedisTrackingSubscriber.class);
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    public RedisTrackingSubscriber(SimpMessagingTemplate messagingTemplate, ObjectMapper objectMapper) {
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Se llama cuando llega un mensaje desde el topic de Redis.
     */
    public void onMessage(String message) {
        try {
            LiveTrackingResponse response = objectMapper.readValue(message, LiveTrackingResponse.class);

            messagingTemplate.convertAndSend(
                    "/topic/tracking/" + response.getMessengerId(),
                    response);
            messagingTemplate.convertAndSend("/topic/tracking/all", response);

            logger.debug("Mensaje de Redis retransmitido vía WebSocket: messengerId={}", response.getMessengerId());
        } catch (Exception e) {
            logger.error("Error al procesar mensaje de Redis Pub/Sub: {}", e.getMessage());
        }
    }
}
