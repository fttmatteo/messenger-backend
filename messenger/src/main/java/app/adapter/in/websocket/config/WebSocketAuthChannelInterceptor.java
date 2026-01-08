package app.adapter.in.websocket.config;

import app.adapter.out.security.JwtAdapter;
import org.springframework.beans.factory.annotation.Autowired;
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

/**
 * Interceptor para autenticar conexiones WebSocket via JWT.
 */
@Component
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    @Autowired
    private JwtAdapter jwtAdapter;

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

            if (token == null) {
                throw new IllegalArgumentException("Token JWT requerido para conexión WebSocket");
            }

            if (!jwtAdapter.validateToken(token)) {
                throw new IllegalArgumentException("Token JWT inválido o expirado");
            }

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
