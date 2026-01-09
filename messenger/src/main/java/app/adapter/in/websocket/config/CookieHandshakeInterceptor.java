package app.adapter.in.websocket.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Interceptor para copiar cookies del handshake HTTP a los atributos de la
 * sesión WebSocket.
 * Esto permite que los ChannelInterceptors accedan a los tokens almacenados en
 * cookies HttpOnly.
 */
public class CookieHandshakeInterceptor implements HandshakeInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(CookieHandshakeInterceptor.class);

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
            WebSocketHandler wsHandler, Map<String, Object> attributes) {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            HttpServletRequest httpRequest = servletRequest.getServletRequest();

            // 1. Intentar extraer del query parameter ?token=... (Crucial para Safari
            // Mobile)
            String tokenParam = httpRequest.getParameter("token");
            if (tokenParam != null && !tokenParam.trim().isEmpty()) {
                attributes.put("accessToken", tokenParam);
                logger.debug("Token encontrado en query parameter 'token' y copiado a atributos de sesión");
            }

            // 2. Intentar extraer de cookies (Soporte tradicional/Chrome)
            Cookie[] cookies = httpRequest.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("accessToken".equals(cookie.getName())) {
                        // Solo sobreescribimos si no se encontró en el query param o para prioridad
                        // En este caso, el query param tiene prioridad para el fix de Safari
                        if (!attributes.containsKey("accessToken")) {
                            attributes.put("accessToken", cookie.getValue());
                            logger.debug("Token accessToken encontrado en cookie y copiado a atributos de sesión");
                        }
                    }
                }
            }
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
            WebSocketHandler wsHandler, Exception exception) {
        // No requiere implementación
    }
}
