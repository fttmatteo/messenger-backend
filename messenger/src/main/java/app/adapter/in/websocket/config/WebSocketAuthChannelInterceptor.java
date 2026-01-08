package app.adapter.in.websocket.config;

import app.adapter.out.security.JwtAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Interceptor para autenticar conexiones WebSocket via JWT.
 * En producción: requiere token válido
 * En desarrollo: permite sin token (facilita testing)
 */
@Component
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketAuthChannelInterceptor.class);

    @Autowired
    private JwtAdapter jwtAdapter;
    
    @Value("${spring.profiles.active:}")
    private String activeProfiles;

    /**
     * Intercepta mensajes entrantes para validar el token JWT en la conexión
     * inicial (CONNECT).
     * Establece la autenticación en el contexto de seguridad del WebSocket.
     */
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = extractToken(accessor);
            boolean isProduction = activeProfiles != null && activeProfiles.contains("prod");

            // Si no hay token, comportamiento diferente según environment
            if (token == null) {
                if (isProduction) {
                    logger.warn("WebSocket connection attempt without JWT token in PRODUCTION - rejecting");
                    throw new IllegalArgumentException("Token JWT requerido para conexión WebSocket");
                } else {
                    logger.warn("WebSocket connection without JWT token - allowed in development");
                    return message;
                }
            }

            // Validar el token si está presente
            if (!jwtAdapter.validateToken(token)) {
                logger.warn("Invalid or expired JWT token for WebSocket connection");
                throw new IllegalArgumentException("Token JWT inválido o expirado");
            }

            // Token válido: establecer autenticación
            String username = jwtAdapter.extractUsername(token);
            String role = jwtAdapter.extractRole(token);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    username,
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + role)));

            accessor.setUser(authentication);
        }

        return message;
    }

    /**
     * Extrae token JWT desde headers STOMP.
     * Prioridad: Authorization -> Cookie: accessToken=...
     */
    private String extractToken(StompHeaderAccessor accessor) {
        // 1) Authorization header (Bearer)
        String authHeader = accessor.getFirstNativeHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        // 2) Cookie header: accessToken=...
        String cookieHeader = accessor.getFirstNativeHeader("Cookie");
        if (cookieHeader != null) {
            return Arrays.stream(cookieHeader.split(";"))
                    .map(String::trim)
                    .filter(c -> c.startsWith("accessToken="))
                    .findFirst()
                    .map(c -> c.substring("accessToken=".length()))
                    .orElse(null);
        }

        return null;
    }
}
