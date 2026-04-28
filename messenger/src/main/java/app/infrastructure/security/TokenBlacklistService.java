package app.infrastructure.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Servicio para blacklist de tokens JWT usando Redis.
 */
@Service
@ConditionalOnProperty(name = "redis.enabled", havingValue = "true", matchIfMissing = true)
public class TokenBlacklistService implements app.domain.ports.TokenBlacklistPort {

    private static final String BLACKLIST_PREFIX = "token:blacklist:";

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * Agrega un token a la lista negra.
     */
    @Override
    public void addToBlacklist(String token, long ttlSeconds) {
        String key = BLACKLIST_PREFIX + hashToken(token);
        redisTemplate.opsForValue().set(key, "revoked", Duration.ofSeconds(ttlSeconds));
    }

    /**
     * Verifica si un token está en la lista negra.
     */
    @Override
    public boolean isBlacklisted(String token) {
        String key = BLACKLIST_PREFIX + hashToken(token);
        Boolean exists = redisTemplate.hasKey(key);
        boolean blacklisted = exists != null && exists;
        return blacklisted;
    }

    /**
     * Genera un hash SHA-256 para el token para evitar almacenar el token completo.
     */
    private String hashToken(String token) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(64);
            for (byte b : hash) {
                int v = b & 0xff;
                sb.append("0123456789abcdef".charAt(v >>> 4));
                sb.append("0123456789abcdef".charAt(v & 0xf));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            // SHA-256 is guaranteed to be available in every JVM
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
