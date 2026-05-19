package app.infrastructure.security;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

/**
 * Filtro de rate limiting basado en IP usando Bucket4j.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitFilter.class);

    @Autowired(required = false)
    private ProxyManager<byte[]> proxyManager;

    private final Map<String, Bucket> localFallbackCache = java.util.Collections.synchronizedMap(
            new java.util.LinkedHashMap<String, Bucket>(101, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, Bucket> eldest) {
                    return size() > 1000; 
                }
            });

    private static final int GENERAL_REQUESTS_PER_MINUTE = 100;
    private static final int AUTH_REQUESTS_PER_MINUTE = 10;

    /**
     * Filtra las peticiones para aplicar el rate limiting.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String clientIp = getClientIP(request);
        String requestUri = request.getRequestURI();

        String key;
        BucketConfiguration config;

        if (requestUri.startsWith("/auth/")) {
            key = "rl_auth_" + clientIp;
            config = authConfig();
        } else {
            key = "rl_gen_" + clientIp;
            config = generalConfig();
        }

        Bucket bucket;
        try {
            if (proxyManager != null) {
                bucket = proxyManager.builder().build(key.getBytes(StandardCharsets.UTF_8), () -> config);
            } else {
                throw new Exception("ProxyManager no disponible");
            }
        } catch (Exception e) {
            logger.error("Redis no está disponible para Rate Limiting. Usando fallback local para IP: {}", clientIp);
            bucket = localFallbackCache.computeIfAbsent(key,
                    k -> Bucket.builder().addLimit(limit -> limit
                            .capacity(key.contains("auth") ? AUTH_REQUESTS_PER_MINUTE : GENERAL_REQUESTS_PER_MINUTE)
                            .refillGreedy(key.contains("auth") ? AUTH_REQUESTS_PER_MINUTE : GENERAL_REQUESTS_PER_MINUTE,
                                    Duration.ofMinutes(1)))
                            .build());
        }

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.setHeader("Retry-After", "60");

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

    private BucketConfiguration authConfig() {
        return BucketConfiguration.builder()
                .addLimit(limit -> limit.capacity(AUTH_REQUESTS_PER_MINUTE)
                        .refillGreedy(AUTH_REQUESTS_PER_MINUTE, Duration.ofMinutes(1)))
                .build();
    }

    private BucketConfiguration generalConfig() {
        return BucketConfiguration.builder()
                .addLimit(limit -> limit.capacity(GENERAL_REQUESTS_PER_MINUTE)
                        .refillGreedy(GENERAL_REQUESTS_PER_MINUTE, Duration.ofMinutes(1)))
                .build();
    }

    private String getClientIP(HttpServletRequest request) {
        String cfIp = request.getHeader("CF-Connecting-IP");
        if (cfIp != null && !cfIp.trim().isEmpty()) {
            return cfIp.trim();
        }
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.trim().isEmpty()) {
            String[] ips = xff.split(",");
            if (ips.length > 0) {
                return ips[0].trim();
            }
        }
        return request.getRemoteAddr();
    }

    /**
     * Determina si una petición debe ser filtrada (excluida del rate limiting).
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator/") ||
                path.startsWith("/swagger-ui/") ||
                path.startsWith("/v3/api-docs");
    }
}
