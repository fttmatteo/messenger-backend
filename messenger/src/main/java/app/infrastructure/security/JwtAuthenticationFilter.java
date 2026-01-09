package app.infrastructure.security;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import app.domain.ports.AuthenticationPort;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Filtro de autenticación JWT para validar tokens en cada request.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private AuthenticationPort authenticationPort;

    /**
     * Filtra las peticiones para validar el token.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Log diagnóstico (solo nombres de cookies) para detectar duplicados en
        // producción
        if (request.getCookies() != null) {
            String cookieNames = Arrays.stream(request.getCookies())
                    .map(Cookie::getName)
                    .collect(Collectors.joining(", "));
            logger.debug("Request a {}: Cookies recibidas: [{}]", request.getRequestURI(), cookieNames);
        }

        String token = this.extractToken(request);
        if (token != null) {
            this.processToken(token);
        }
        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        // PRIORIDAD 1: Intentar leer token de cookie (más seguro)
        if (request.getCookies() != null) {
            List<String> potentialTokens = Arrays.stream(request.getCookies())
                    .filter(c -> "accessToken".equals(c.getName()))
                    .map(Cookie::getValue)
                    .filter(v -> v != null && !v.trim().isEmpty())
                    .toList();

            if (potentialTokens.size() > 1) {
                logger.warn("Se detectaron {} cookies 'accessToken'. Probando validación...", potentialTokens.size());
            }

            for (String val : potentialTokens) {
                if (authenticationPort.validateToken(val)) {
                    return val;
                }
            }

            // Si ninguna fue válida pero había cookies, retornamos null (silencioso)
            if (!potentialTokens.isEmpty()) {
                return null;
            }
        }

        // FALLBACK: Leer de header Authorization (compatibilidad con clientes legacy)
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer")) {
            String value = header.substring(7).trim();
            if (value.isEmpty()) {
                return null;
            }
            return value;
        }

        return null;
    }

    private void processToken(String token) {
        if (authenticationPort.validateToken(token)) {
            String username = authenticationPort.extractUsername(token);
            String role = authenticationPort.extractRole(token);
            if (role == null || role.trim().isEmpty()) {
                return;
            }
            String normalized = role.trim();
            if (!normalized.toUpperCase().startsWith("ROLE_")) {
                normalized = "ROLE_" + normalized.toUpperCase();
            } else {
                normalized = normalized.toUpperCase();
            }
            ArrayList<SimpleGrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority(normalized));
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    username,
                    null,
                    authorities);
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        // Token inválido: no autenticar al usuario silenciosamente.
        // Spring Security denegará acceso a endpoints protegidos con 401/403.
    }
}