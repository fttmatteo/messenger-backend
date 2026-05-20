package app.domain.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import app.domain.util.LogSanitizer;
import java.util.concurrent.TimeUnit;

/**
 * Servicio de rate limiting para proteger contra ataques de fuerza bruta en login.
 * Limita intentos fallidos por documento y por IP.
 * 
 * Solo se carga cuando redis.enabled=true.
 * Cuando redis.enabled=false, se usa NoOpLoginRateLimitService en su lugar.
 */
@Service
@ConditionalOnProperty(name = "redis.enabled", havingValue = "true", matchIfMissing = true)
public class LoginRateLimitService {

    private static final Logger logger = LoggerFactory.getLogger(LoginRateLimitService.class);

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int BLOCK_DURATION_MINUTES = 15;
    private static final String LOGIN_ATTEMPT_PREFIX = "login:attempt:";
    private static final String LOGIN_BLOCK_PREFIX = "login:block:";

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * Verifica si una cuenta está bloqueada por demasiados intentos fallidos.
     */
    public boolean isBlocked(Long document) {
        String blockKey = LOGIN_BLOCK_PREFIX + document;
        return Boolean.TRUE.equals(redisTemplate.hasKey(blockKey));
    }

    /**
     * Registra un intento de login fallido.
     * Si se exceden los intentos máximos, bloquea la cuenta.
     * Retorna el número de intentos restantes.
     */
    public int recordFailedAttempt(Long document) {
        String attemptKey = LOGIN_ATTEMPT_PREFIX + document;
        
        Long attempts = redisTemplate.opsForValue().increment(attemptKey);
        
        if (attempts == 1) {
            redisTemplate.expire(attemptKey, BLOCK_DURATION_MINUTES, TimeUnit.MINUTES);
        }

        logger.info("[Seguridad] Intento fallido de login ({}/{}) para documento {}", 
                attempts, MAX_FAILED_ATTEMPTS, LogSanitizer.maskDocument(document));
        
        if (attempts >= MAX_FAILED_ATTEMPTS) {
            String blockKey = LOGIN_BLOCK_PREFIX + document;
            redisTemplate.opsForValue().set(blockKey, "blocked", BLOCK_DURATION_MINUTES, TimeUnit.MINUTES);
            logger.warn("[Seguridad] Documento {} bloqueado por {} minutos debido a demasiados intentos fallidos", 
                    LogSanitizer.maskDocument(document), BLOCK_DURATION_MINUTES);
        }
        
        return Math.max(0, MAX_FAILED_ATTEMPTS - (int)attempts.longValue());
    }

    /**
     * Limpia los intentos fallidos después de un login exitoso.
     */
    public void clearFailedAttempts(Long document) {
        String attemptKey = LOGIN_ATTEMPT_PREFIX + document;
        redisTemplate.delete(attemptKey);
    }

    /**
     * Obtiene el número de intentos fallidos restantes.
     */
    public int getRemainingAttempts(Long document) {
        String attemptKey = LOGIN_ATTEMPT_PREFIX + document;
        Object value = redisTemplate.opsForValue().get(attemptKey);
        if (value == null) {
            return MAX_FAILED_ATTEMPTS;
        }
        int attempts = Integer.parseInt(value.toString());
        return Math.max(0, MAX_FAILED_ATTEMPTS - attempts);
    }
}
