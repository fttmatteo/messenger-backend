package app.infrastructure.persistence.adapter;

import app.domain.ports.WhatsAppRateLimitPort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Adaptador de Redis para el rate limiting de WhatsApp.
 * Implementa el bloqueo distribuido de PIN.
 */
@Component
public class RedisWhatsAppRateLimitAdapter implements WhatsAppRateLimitPort {

    private static final String ATTEMPT_PREFIX = "wa:pin:attempts:";
    private static final String BLOCK_PREFIX = "wa:pin:block:";
    private static final int MAX_ATTEMPTS = 3;
    private static final int BLOCK_DURATION_MINUTES = 15;

    private final RedisTemplate<String, String> redisTemplate;

    public RedisWhatsAppRateLimitAdapter(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean isBlocked(String phoneNumber) {
        String blockKey = BLOCK_PREFIX + phoneNumber;
        return Boolean.TRUE.equals(redisTemplate.hasKey(blockKey));
    }

    @Override
    public int recordFailedAttempt(String phoneNumber) {
        String attemptKey = ATTEMPT_PREFIX + phoneNumber;

        Long attempts = redisTemplate.opsForValue().increment(attemptKey);

        if (attempts != null && attempts == 1) {
            redisTemplate.expire(attemptKey, BLOCK_DURATION_MINUTES, TimeUnit.MINUTES);
        }

        int currentAttempts = (attempts != null) ? attempts.intValue() : 0;

        if (currentAttempts >= MAX_ATTEMPTS) {
            String blockKey = BLOCK_PREFIX + phoneNumber;
            redisTemplate.opsForValue().set(blockKey, "blocked", BLOCK_DURATION_MINUTES, TimeUnit.MINUTES);
            redisTemplate.delete(attemptKey); // Limpiar intentos al bloquear
        }

        return Math.max(0, MAX_ATTEMPTS - currentAttempts);
    }

    @Override
    public void clearFailedAttempts(String phoneNumber) {
        redisTemplate.delete(ATTEMPT_PREFIX + phoneNumber);
    }
}
