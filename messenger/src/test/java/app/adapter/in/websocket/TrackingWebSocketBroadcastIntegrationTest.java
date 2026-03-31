package app.adapter.in.websocket;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import app.adapter.in.rest.response.LiveTrackingResponse;
import app.support.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Pruebas de integración para el bridge Redis Pub/Sub -> WebSocket.
 * Verifica que los mensajes publicados en Redis lleguen a los tópicos de WebSocket.
 */
@DisplayName("Tracking WebSocket Broadcast Integration Tests")
class TrackingWebSocketBroadcastIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @MockitoBean
    private SimpMessagingTemplate messagingTemplate;

    @Test
    @DisplayName("Should broadcast Redis message to WebSocket topics")
    void shouldBroadcastToWebSockets() {
        // 1. Preparar mensaje JSON (LiveTrackingResponse)
        String jsonMessage = """
            {
                "messengerId": 999,
                "latitude": 4.6097,
                "longitude": -74.0817,
                "accuracy": 10.0,
                "status": "ACTIVE",
                "timestamp": "2026-03-31T01:00:00"
            }
            """;

        // 2. Publicar en el topic de Redis que escucha la aplicación
        redisTemplate.convertAndSend("tracking:updates", jsonMessage);

        // 3. Verificar que el RedisTrackingSubscriber capturó el mensaje
        // y lo retransmitió vía SimpMessagingTemplate.
        // Usamos timeout por ser asíncrono.
        verify(messagingTemplate, timeout(5000)).convertAndSend(
                eq("/topic/tracking/999"),
                org.mockito.ArgumentMatchers.any(LiveTrackingResponse.class)
        );

        verify(messagingTemplate, timeout(5000)).convertAndSend(
                eq("/topic/tracking/all"),
                org.mockito.ArgumentMatchers.any(LiveTrackingResponse.class)
        );
    }
}
