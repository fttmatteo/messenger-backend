package app.adapter.out.persistence.adapter;

import app.domain.ports.StorageCachePort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Adapter que implementa el caché de almacenamiento usando Redis.
 */
@Component
public class RedisStorageCacheAdapter implements StorageCachePort {

    private static final String CACHE_PREFIX = "storage:url:";
    private final StringRedisTemplate redisTemplate;

    public RedisStorageCacheAdapter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Optional<String> getUrl(String objectName) {
        String url = redisTemplate.opsForValue().get(CACHE_PREFIX + objectName);
        return Optional.ofNullable(url);
    }

    @Override
    public void cacheUrl(String objectName, String url, long expirationSeconds) {
        redisTemplate.opsForValue().set(CACHE_PREFIX + objectName, url, expirationSeconds, TimeUnit.SECONDS);
    }

    @Override
    public void evictUrl(String objectName) {
        redisTemplate.delete(CACHE_PREFIX + objectName);
    }
}
