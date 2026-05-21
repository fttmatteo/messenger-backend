package app.adapter.in.websocket.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import java.util.Map;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Interceptor para copiar cookies del handshake HTTP a los atributos de la
 * sesión WebSocket.
 * Esto permite que los ChannelInterceptors accedan a los tokens almacenados en
 * cookies HttpOnly.
 */
@Component
public class CookieHandshakeInterceptor implements HandshakeInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(CookieHandshakeInterceptor.class);

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
            WebSocketHandler wsHandler, Map<String, Object> attributes) {

        logger.info("Iniciando WebSocket Handshake.");

        if (request instanceof ServletServerHttpRequest servletRequest) {
            HttpServletRequest httpRequest = servletRequest.getServletRequest();

            String tokenParam = httpRequest.getParameter("token");
            if (tokenParam == null || tokenParam.trim().isEmpty()) {
                String query = request.getURI().getQuery();
                if (query != null && query.contains("token=")) {
                    tokenParam = Arrays.stream(query.split("&"))
                            .filter(s -> s.startsWith("token="))
                            .map(s -> s.substring(6))
                            .findFirst()
                            .orElse(null);
                    logger.debug("Token extraído manualmente del Query String: {}",
                            tokenParam != null ? "PRESENTE" : "NULO");
                }
            }

            if (tokenParam != null && !tokenParam.trim().isEmpty()) {
                attributes.put("accessToken", tokenParam.trim());
                logger.info("Token de acceso encontrado en query param y copiado a atributos de sesión");
            }

            Cookie[] cookies = httpRequest.getCookies();
            if (cookies != null) {
                logger.debug("Handshake: encontradas {} cookies", cookies.length);
                for (Cookie cookie : cookies) {
                    if ("accessToken".equals(cookie.getName())) {
                        if (!attributes.containsKey("accessToken")) {
                            attributes.put("accessToken", cookie.getValue());
                            logger.info("Token accessToken encontrado en cookie y copiado a atributos de sesión");
                        }
                    }
                }
            } else {
                logger.debug("Handshake: No se encontraron cookies en el request");
            }
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
            WebSocketHandler wsHandler, Exception exception) {
    }
}
