package app.infrastructure.security;

import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final Map<String, Bucket> bucketCache = new ConcurrentHashMap<>();
    private final Map<String, Bucket> authBucketCache = new ConcurrentHashMap<>();

    private static final int GENERAL_REQUESTS_PER_MINUTE = 100;
    private static final int AUTH_REQUESTS_PER_MINUTE = 10;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String clientIp = getClientIP(request);
        String requestUri = request.getRequestURI();

        Bucket bucket;

        if (requestUri.startsWith("/auth/")) {
            bucket = authBucketCache.computeIfAbsent(clientIp, this::createAuthBucket);
        } else {
            bucket = bucketCache.computeIfAbsent(clientIp, this::createGeneralBucket);
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

    private Bucket createAuthBucket(String ip) {
        return Bucket.builder()
                .addLimit(limit -> limit.capacity(AUTH_REQUESTS_PER_MINUTE)
                        .refillGreedy(AUTH_REQUESTS_PER_MINUTE, Duration.ofMinutes(1)))
                .build();
    }

    private Bucket createGeneralBucket(String ip) {
        return Bucket.builder()
                .addLimit(limit -> limit.capacity(GENERAL_REQUESTS_PER_MINUTE)
                        .refillGreedy(GENERAL_REQUESTS_PER_MINUTE, Duration.ofMinutes(1)))
                .build();
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null && !xfHeader.isEmpty()) {
            return xfHeader.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator/") ||
                path.startsWith("/swagger-ui/") ||
                path.startsWith("/v3/api-docs");
    }
}
