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
 * Configuración de WebSocket con STOMP para tracking de mensajeros.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${websocket.allowed.origins}")
    private String allowedOrigins;
    @Autowired
    private WebSocketAuthChannelInterceptor authChannelInterceptor;
    @Autowired
    private CookieHandshakeInterceptor cookieHandshakeInterceptor;

    /**
     * Habilita un broker de mensajes en memoria simple.
     * Configura los prefijos para destinos de aplicación y usuario.
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    /**
     * Registra el interceptor de autenticación para canales de entrada.
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(authChannelInterceptor);
    }

    /**
     * Registra el endpoint STOMP para conexiones WebSocket.
     * Configura CORS basado en propiedades de aplicación.
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        String[] origins = allowedOrigins.split(",");
        for (int i = 0; i < origins.length; i++) {
            origins[i] = origins[i].trim();
        }

        registry.addEndpoint("/ws/tracking")
                .setAllowedOriginPatterns(origins)
                .addInterceptors(cookieHandshakeInterceptor)
                .withSockJS();
        registry.addEndpoint("/ws/tracking")
                .setAllowedOriginPatterns(origins)
                .addInterceptors(cookieHandshakeInterceptor);
    }
}
