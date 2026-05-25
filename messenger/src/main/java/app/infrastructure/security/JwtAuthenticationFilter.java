package app.infrastructure.security;

import java.util.ArrayList;
import java.util.Arrays;
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

        if (request.getCookies() != null) {
            String cookieNames = Arrays.stream(request.getCookies())
                    .map(Cookie::getName)
                    .collect(Collectors.joining(", "));
            logger.debug("Request a {}: Cookies recibidas: [{}]", request.getRequestURI(), cookieNames);
        } else {
            if (isProtectedRoute(request)) {
                logger.debug("Request a {}: NO se recibieron cookies en una ruta protegida.", request.getRequestURI());
            }
        }

        String token = this.extractToken(request);
        if (token != null) {
            this.processToken(token);
        }
        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("accessToken".equals(cookie.getName())) {
                    String value = cookie.getValue();
                    if (value != null && !value.trim().isEmpty() && authenticationPort.validateToken(value)) {
                        return value;
                    }
                }
            }
        }

        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer")) {
            String value = header.substring(7).trim();
            if (!value.isEmpty() && authenticationPort.validateToken(value)) {
                logger.debug("Token válido encontrado en header Authorization");
                return value;
            }
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
    }

    private boolean isProtectedRoute(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !path.startsWith("/auth/") &&
                !path.startsWith("/swagger-ui") &&
                !path.startsWith("/v3/api-docs") &&
                !path.equals("/actuator/health") &&
                !path.startsWith("/ws/");
    }
}