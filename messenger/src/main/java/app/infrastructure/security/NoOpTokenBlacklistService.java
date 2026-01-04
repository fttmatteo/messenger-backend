package app.infrastructure.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * No-op implementation of TokenBlacklistPort for test environments without
 * Redis.
 * This is used when redis.enabled=false.
 * In tests, token blacklist is not enforced.
 */
@Service
@ConditionalOnProperty(name = "redis.enabled", havingValue = "false")
public class NoOpTokenBlacklistService implements app.domain.ports.TokenBlacklistPort {

    private static final Logger logger = LoggerFactory.getLogger(NoOpTokenBlacklistService.class);

    /**
     * Agrega un token a la lista negra (no-op en este caso).
     */
    @Override
    public void addToBlacklist(String token, long ttlSeconds) {
        logger.debug("NoOp: addToBlacklist called (Redis disabled)");
    }

    /**
     * Verifica si un token está en la lista negra (no-op en este caso).
     */
    @Override
    public boolean isBlacklisted(String token) {
        logger.debug("NoOp: isBlacklisted called (Redis disabled)");
        return false;
    }
}
