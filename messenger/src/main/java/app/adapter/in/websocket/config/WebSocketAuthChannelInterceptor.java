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
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new IllegalArgumentException("Token JWT requerido para conexión WebSocket");
            }

            String token = authHeader.substring(7);

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
}
