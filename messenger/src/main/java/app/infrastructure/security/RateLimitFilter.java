package app.infrastructure.security;

import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Filtro de Rate Limiting para proteger la API contra abusos.
 * 
 * Implementa el algoritmo Token Bucket para limitar el número de peticiones
 * por IP en un intervalo de tiempo determinado.
 * 
 * Límites configurados:
 * - Endpoints de autenticación (/auth/**): 10 requests por minuto (protección
 * brute force)
 * - API general: 100 requests por minuto por IP
 * 
 * Cuando se excede el límite:
 * - Responde con HTTP 429 Too Many Requests
 * - Incluye header Retry-After indicando segundos hasta próximo request
 * permitido
 * 
 * Seguridad:
 * - Usa buckets separados para auth y API general
 * - Cache en memoria (ConcurrentHashMap) para alto rendimiento
 * - Limpieza automática de IPs inactivas (TODO: implementar con TTL)
 * 
 * @see io.github.bucket4j.Bucket
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitFilter.class);

    // Cache de buckets por IP para rate limiting general
    private final Map<String, Bucket> bucketCache = new ConcurrentHashMap<>();

    // Cache de buckets por IP para rate limiting de autenticación (más estricto)
    private final Map<String, Bucket> authBucketCache = new ConcurrentHashMap<>();

    // Límites de rate limiting
    private static final int GENERAL_REQUESTS_PER_MINUTE = 100;
    private static final int AUTH_REQUESTS_PER_MINUTE = 10;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String clientIp = getClientIP(request);
        String requestUri = request.getRequestURI();

        // Determinar qué bucket usar basado en el endpoint
        Bucket bucket;
        String limitType;

        if (requestUri.startsWith("/auth/")) {
            // Rate limiting estricto para endpoints de autenticación
            bucket = authBucketCache.computeIfAbsent(clientIp, this::createAuthBucket);
            limitType = "AUTH";
        } else {
            // Rate limiting general para el resto de la API
            bucket = bucketCache.computeIfAbsent(clientIp, this::createGeneralBucket);
            limitType = "GENERAL";
        }

        // Intentar consumir un token
        if (bucket.tryConsume(1)) {
            // Request permitido
            filterChain.doFilter(request, response);
        } else {
            // Rate limit excedido
            logger.warn("RATE LIMIT: {} excedido para IP {} en {}", limitType, clientIp, requestUri);

            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.setHeader("Retry-After", "60"); // Reintentar en 60 segundos

            String errorJson = """
                    {
                        "status": 429,
                        "error": "Too Many Requests",
                        "message": "Demasiadas peticiones. Por favor espere antes de intentar nuevamente.",
                        "path": "%s"
                    }
                    """.formatted(requestUri);

            response.getWriter().write(errorJson);
        }
    }

    /**
     * Crea un bucket para rate limiting de autenticación.
     * Límite: 10 requests por minuto (protección contra brute force).
     */
    private Bucket createAuthBucket(String ip) {
        return Bucket.builder()
                .addLimit(limit -> limit.capacity(AUTH_REQUESTS_PER_MINUTE)
                        .refillGreedy(AUTH_REQUESTS_PER_MINUTE, Duration.ofMinutes(1)))
                .build();
    }

    /**
     * Crea un bucket para rate limiting general de la API.
     * Límite: 100 requests por minuto.
     */
    private Bucket createGeneralBucket(String ip) {
        return Bucket.builder()
                .addLimit(limit -> limit.capacity(GENERAL_REQUESTS_PER_MINUTE)
                        .refillGreedy(GENERAL_REQUESTS_PER_MINUTE, Duration.ofMinutes(1)))
                .build();
    }

    /**
     * Obtiene la IP real del cliente.
     * Considera headers de proxy (X-Forwarded-For) para obtener la IP original.
     */
    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null && !xfHeader.isEmpty()) {
            // X-Forwarded-For puede contener múltiples IPs, la primera es la del cliente
            return xfHeader.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * Excluir endpoints que no requieren rate limiting.
     * Por ejemplo: health checks, swagger, etc.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator/") ||
                path.startsWith("/swagger-ui/") ||
                path.startsWith("/v3/api-docs");
    }
}
