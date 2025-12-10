package app.adapter.in.websocket.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configuración de WebSocket con STOMP para tracking en tiempo real.
 * 
 * Arquitectura de mensajes:
 * - /app/tracking/update → Mensajeros envían ubicaciones
 * - /topic/tracking/{messengerId} → Admins se suscriben para recibir
 * actualizaciones
 * - /topic/tracking/all → Admins reciben todas las actualizaciones (broadcast)
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${websocket.allowed.origins}")
    private String allowedOrigins;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Habilitar broker simple para topics y queues
        registry.enableSimpleBroker("/topic", "/queue");

        // Prefijo para mensajes enviados desde el cliente al servidor
        registry.setApplicationDestinationPrefixes("/app");

        // Prefijo para mensajes destinados a usuarios específicos
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint principal para conexión WebSocket
        registry.addEndpoint("/ws/tracking")
                .setAllowedOriginPatterns(allowedOrigins.split(","))
                .withSockJS(); // Fallback para navegadores sin WebSocket nativo

        // Endpoint sin SockJS (para apps móviles o clientes nativos)
        registry.addEndpoint("/ws/tracking")
                .setAllowedOriginPatterns(allowedOrigins.split(","));
    }
}
