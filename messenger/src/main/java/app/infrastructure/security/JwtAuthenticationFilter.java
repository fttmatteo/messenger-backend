package app.infrastructure.security;

import java.io.IOException;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import app.domain.ports.AuthenticationPort;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Filtro de seguridad que intercepta cada petición HTTP para autenticación JWT.
 * 
 * Extiende OncePerRequestFilter de Spring para garantizar que se ejecute
 * exactamente una vez por petición, incluso en caso de forwards o includes.
 * 
 * Flujo de procesamiento:
 * 1. Extrae el token JWT del header "Authorization: Bearer {token}"
 * 2. Valida el token usando AuthenticationPort
 * 3. Extrae username y role del token
 * 4. Normaliza el role añadiendo prefijo "ROLE_" si es necesario
 * 5. Crea Authentication y lo establece en SecurityContext
 * 6. Permite que la petición continúe en la cadena de filtros
 * 
 * Si el token es inválido o no existe, la petición continúa pero sin
 * contexto de autenticación (será rechazada por rutas protegidas).
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private AuthenticationPort authenticationPort;

    /**
     * Método principal del filtro que se ejecuta en cada petición HTTP.
     * 
     * Proceso:
     * 1. Extrae el token JWT del header Authorization
     * 2. Si existe token, lo procesa y valida
     * 3. Continúa la cadena de filtros (independientemente del resultado)
     * 
     * Este filtro NO detiene la petición si el token es inválido,
     * simplemente no establece el contexto de autenticación.
     * La autorización real se maneja en SecurityConfig.
     * 
     * @param request     Petición HTTP entrante
     * @param response    Respuesta HTTP saliente
     * @param filterChain Cadena de filtros de Spring Security
     * @throws ServletException Si hay error en el procesamiento del servlet
     * @throws IOException      Si hay error de I/O
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = this.extractToken(request);
        if (token != null) {
            logger.debug("Token JWT detectado en request: {}", request.getRequestURI());
            this.processToken(token);
        }
        filterChain.doFilter(request, response);
    }

    /**
     * Extrae el token JWT del header Authorization de la petición.
     * 
     * Formato esperado: "Authorization: Bearer {token}"
     * 
     * Proceso:
     * 1. Obtiene el header "Authorization"
     * 2. Verifica que comience con "Bearer "
     * 3. Extrae el token eliminando el prefijo "Bearer " (7 caracteres)
     * 
     * @param request Petición HTTP de la cual extraer el token
     * @return Token JWT sin el prefijo "Bearer ", o null si no existe header válido
     */
    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer")) {
            return header.substring(7);
        }
        return null;
    }

    /**
     * Procesa y valida el token JWT, estableciendo el contexto de seguridad.
     * 
     * Proceso detallado:
     * 1. Valida el token JWT (firma, expiración, formato)
     * 2. Extrae username del token
     * 3. Extrae role del token
     * 4. Normaliza el role:
     * - Trim de espacios
     * - Añade prefijo "ROLE_" si no existe
     * - Convierte a mayúsculas
     * Ejemplos: "admin" -> "ROLE_ADMIN", "ROLE_messenger" -> "ROLE_MESSENGER"
     * 5. Crea lista de authorities con el role normalizado
     * 6. Crea UsernamePasswordAuthenticationToken con username y authorities
     * 7. Establece el token en SecurityContextHolder para la petición actual
     * 
     * Si el token es inválido o el role es vacío, no hace nada (no autentica).
     * 
     * @param token Token JWT a procesar y validar
     */
    private void processToken(String token) {
        if (authenticationPort.validateToken(token)) {
            String username = authenticationPort.extractUsername(token);
            String role = authenticationPort.extractRole(token);
            if (role == null || role.trim().isEmpty()) {
                logger.warn("Token válido pero sin role para usuario: {}", username);
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
            logger.debug("Usuario autenticado: {} con role: {}", username, normalized);
        } else {
            logger.warn("Token JWT inválido o expirado");
        }
    }
}