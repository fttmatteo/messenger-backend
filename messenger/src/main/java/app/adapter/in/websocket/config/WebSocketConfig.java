package app.adapter.in.websocket.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configuración de WebSocket con protocolo STOMP para comunicación en tiempo
 * real.
 * 
 * Esta clase configura la infraestructura de WebSocket del sistema, habilitando
 * la comunicación bidireccional en tiempo real entre clientes (mensajeros y
 * administradores)
 * y el servidor para el rastreo de ubicaciones.
 * 
 * Configuración del Message Broker:
 * - Broker simple habilitado para /topic (broadcast) y /queue (mensajes punto a
 * punto)
 * - Prefijo de aplicación: /app (para mensajes del cliente al servidor)
 * - Prefijo de usuario: /user (para mensajes dirigidos a usuarios específicos)
 * 
 * Endpoints STOMP configurados:
 * - /ws/tracking: Endpoint principal de conexión WebSocket
 * - Con SockJS: Proporciona fallback para navegadores sin soporte WebSocket
 * nativo
 * - Sin SockJS: Para clientes nativos (apps móviles) que soportan WebSocket
 * directamente
 * 
 * Arquitectura de canales:
 * - /app/tracking/update: Mensajeros envían ubicaciones
 * - /topic/tracking/{messengerId}: Admins reciben actualizaciones de un
 * mensajero específico
 * - /topic/tracking/all: Admins reciben actualizaciones de todos los mensajeros
 * (broadcast)
 * 
 * Seguridad:
 * - Los orígenes permitidos se configuran mediante la propiedad
 * websocket.allowed.origins
 * - Soporta múltiples orígenes separados por comas
 * 
 * @see TrackingWebSocketController
 * @see org.springframework.messaging.simp.config.MessageBrokerRegistry
 * @see org.springframework.web.socket.config.annotation.StompEndpointRegistry
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
