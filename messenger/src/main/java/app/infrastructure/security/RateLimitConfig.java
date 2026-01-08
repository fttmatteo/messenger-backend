package app.infrastructure.security;

import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.distributed.proxy.ClientSideConfig;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import java.time.Duration;

/**
 * Configuración de Bucket4j para Rate Limiting distribuido usando Redis
 * (Lettuce).
 */
@Configuration
public class RateLimitConfig {

    /**
     * Crea un ProxyManager distribuido basado en Lettuce.
     * Permite que múltiples instancias compartan los limites de rate limiting.
     */
    @Bean
    public ProxyManager<byte[]> lettuceProxyManager(RedisConnectionFactory connectionFactory) {
        if (!(connectionFactory instanceof LettuceConnectionFactory)) {
            throw new IllegalStateException("Se requiere LettuceConnectionFactory para el Rate Limiting con Redis.");
        }

        LettuceConnectionFactory lettuceFactory = (LettuceConnectionFactory) connectionFactory;

        // Obtenemos el cliente nativo de Lettuce desde la factoría de Spring
        RedisClient redisClient = (RedisClient) lettuceFactory.getNativeClient();

        if (redisClient == null) {
            throw new IllegalStateException("No se pudo obtener el cliente nativo de Redis (Lettuce).");
        }

        // Configuración de expiración para las llaves en Redis.
        // Evita que Redis se llene con IPs que solo se vieron una vez.
        ClientSideConfig clientSideConfig = ClientSideConfig.getDefault()
                .withExpirationAfterWriteStrategy(
                        ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(Duration.ofMinutes(1)));

        return LettuceBasedProxyManager.builderFor(redisClient)
                .withClientSideConfig(clientSideConfig)
                .build();
    }
}
