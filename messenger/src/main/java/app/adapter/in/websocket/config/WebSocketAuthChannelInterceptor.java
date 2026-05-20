package app.adapter.in.websocket.config;

import app.adapter.out.security.JwtAdapter;
import app.domain.util.LogSanitizer;
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
            if (accessor.getUser() != null) {
                logger.debug("Conexión WebSocket ya autenticada vía Principal para usuario: {}",
                        LogSanitizer.maskDocument(accessor.getUser().getName()));
                return message;
            }

            String token = extractToken(accessor);
            boolean isProduction = activeProfiles != null && activeProfiles.contains("prod");

            if (token == null) {
                if (isProduction) {
                    logger.warn("[Seguridad] WebSocket CONNECT sin JWT en producción - rechazado. SessionID: {}",
                            accessor.getSessionId());
                    throw new IllegalArgumentException("Token JWT requerido para conexión WebSocket");
                } else {
                    logger.debug("WebSocket CONNECT sin JWT - permitido en desarrollo");
                    return message;
                }
            }

            if (!jwtAdapter.validateToken(token)) {
                logger.error("Token JWT inválido o expirado en WebSocket. Token: {} | Session: {}",
                    LogSanitizer.maskToken(token), accessor.getSessionId());
                throw new IllegalArgumentException("Token JWT inválido o expirado");
            }

            String username = jwtAdapter.extractUsername(token);
            String role = jwtAdapter.extractRole(token);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    username,
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + role)));

            accessor.setUser(authentication);
            logger.info("Conexión WebSocket exitosa para usuario: {} con rol: {}", LogSanitizer.maskDocument(username),
                    role);
        }

        return message;
    }

    /**
     * Extrae token JWT desde headers STOMP o atributos de sesión.
     * Prioridad: Authorization Header -> Session Attribute (Cookies) -> Native
     * Cookie Header
     */
    private String extractToken(StompHeaderAccessor accessor) {
        String authHeader = accessor.getFirstNativeHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7).trim();
            if (!token.isEmpty())
                return token;
        }

        if (accessor.getSessionAttributes() != null) {
            String token = (String) accessor.getSessionAttributes().get("accessToken");
            if (token != null && !token.isEmpty())
                return token;
        }

        String cookieHeader = accessor.getFirstNativeHeader("Cookie");
        if (cookieHeader != null) {
            return Arrays.stream(cookieHeader.split(";"))
                    .map(String::trim)
                    .filter(c -> c.startsWith("accessToken="))
                    .map(c -> c.substring("accessToken=".length()).trim())
                    .filter(token -> !token.isEmpty())
                    .findFirst()
                    .orElse(null);
        }

        return null;
    }
}
