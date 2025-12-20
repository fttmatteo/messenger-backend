package app.infrastructure.health;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

/**
 * Indicador de salud para verificar conectividad con Redis.
 */
@Component
public class RedisHealthIndicator implements HealthIndicator {

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @Override
    public Health health() {
        try (RedisConnection connection = redisConnectionFactory.getConnection()) {
            String pong = connection.ping();
            if ("PONG".equals(pong)) {
                return Health.up()
                        .withDetail("service", "Redis")
                        .withDetail("response", pong)
                        .build();
            } else {
                return Health.down()
                        .withDetail("service", "Redis")
                        .withDetail("error", "Unexpected response: " + pong)
                        .build();
            }
        } catch (Exception e) {
            return Health.down()
                    .withDetail("service", "Redis")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
