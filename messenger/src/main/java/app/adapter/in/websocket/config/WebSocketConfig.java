package app.adapter.in.websocket.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
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
 * administradores) y el servidor para el rastreo de ubicaciones.
 * 
 * Seguridad:
 * - Autenticación JWT obligatoria en conexión STOMP (via
 * WebSocketAuthChannelInterceptor)
 * - Los orígenes permitidos se configuran mediante websocket.allowed.origins
 * - Rechaza conexiones sin token válido
 * 
 * Configuración del Message Broker:
 * - Broker simple habilitado para /topic (broadcast) y /queue (punto a punto)
 * - Prefijo de aplicación: /app (mensajes del cliente al servidor)
 * - Prefijo de usuario: /user (mensajes dirigidos a usuarios específicos)
 * 
 * @see WebSocketAuthChannelInterceptor
 * @see TrackingWebSocketController
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${websocket.allowed.origins}")
    private String allowedOrigins;

    @Autowired
    private WebSocketAuthChannelInterceptor authChannelInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Habilitar broker simple para topics y queues
        registry.enableSimpleBroker("/topic", "/queue");

        // Prefijo para mensajes enviados desde el cliente al servidor
        registry.setApplicationDestinationPrefixes("/app");

        // Prefijo para mensajes destinados a usuarios específicos
        registry.setUserDestinationPrefix("/user");
    }

    /**
     * Configura interceptores en el canal de entrada de mensajes.
     * 
     * Registra el interceptor de autenticación JWT para validar tokens
     * en conexiones STOMP CONNECT.
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(authChannelInterceptor);
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
