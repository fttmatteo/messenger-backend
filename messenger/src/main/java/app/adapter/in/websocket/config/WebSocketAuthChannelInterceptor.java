package app.adapter.in.websocket.config;

import app.adapter.out.security.JwtAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * Interceptor de canal para autenticación JWT en conexiones WebSocket.
 * 
 * Este interceptor valida tokens JWT durante el handshake STOMP (comando
 * CONNECT),
 * rechazando conexiones sin token válido.
 * 
 * Seguridad:
 * - Valida token JWT en el header "Authorization" durante CONNECT
 * - Extrae username y rol del token para establecer el contexto de seguridad
 * - Rechaza conexiones sin token o con token inválido/expirado
 * - Previene ataques Cross-Site WebSocket Hijacking (CSWSH)
 * 
 * Uso:
 * El cliente debe enviar el header "Authorization: Bearer {token}" en el
 * frame CONNECT de STOMP.
 * 
 * @see WebSocketConfig
 * @see JwtAdapter
 */
@Component
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketAuthChannelInterceptor.class);

    @Autowired
    private JwtAdapter jwtAdapter;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            // Obtener el header de autorización
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                logger.warn("SEGURIDAD: Intento de conexión WebSocket sin token JWT");
                throw new IllegalArgumentException("Token JWT requerido para conexión WebSocket");
            }

            String token = authHeader.substring(7);

            // Validar token
            if (!jwtAdapter.validateToken(token)) {
                logger.warn("SEGURIDAD: Intento de conexión WebSocket con token inválido");
                throw new IllegalArgumentException("Token JWT inválido o expirado");
            }

            // Extraer información del usuario
            String username = jwtAdapter.extractUsername(token);
            String role = jwtAdapter.extractRole(token);

            logger.debug("WebSocket CONNECT autorizado para usuario: {} con rol: {}", username, role);

            // Crear autenticación y establecerla en el accessor
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    username,
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + role)));

            accessor.setUser(authentication);
        }

        return message;
    }
}
