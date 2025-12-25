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

    public void addToBlacklist(String token, long ttlSeconds) {
        String key = BLACKLIST_PREFIX + hashToken(token);
        redisTemplate.opsForValue().set(key, "revoked", Duration.ofSeconds(ttlSeconds));
    }

    public boolean isBlacklisted(String token) {
        String key = BLACKLIST_PREFIX + hashToken(token);
        Boolean exists = redisTemplate.hasKey(key);
        boolean blacklisted = exists != null && exists;
        return blacklisted;
    }

    private String hashToken(String token) {
        if (token.length() <= 32) {
            return token;
        }
        return token.substring(token.length() - 32);
    }
}
