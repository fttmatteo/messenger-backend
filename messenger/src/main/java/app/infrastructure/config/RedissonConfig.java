package app.infrastructure.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Configuración de Redisson para Hibernate L2 Cache.
 * 
 * Usa las mismas variables de entorno que spring.data.redis para
 * garantizar compatibilidad con Redis Cloud y cualquier proveedor de Redis.
 */
@Configuration
@Profile("!test")
public class RedissonConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Value("${spring.data.redis.password:}")
    private String redisPassword;

    @Value("${spring.data.redis.lettuce.pool.max-active:4}")
    private int maxActive;

    @Value("${spring.data.redis.lettuce.pool.min-idle:1}")
    private int minIdle;

    @Value("${spring.data.redis.timeout:5000}")
    private int timeout;

    /**
     * Crea el cliente Redisson configurado para conectar a Redis Cloud
     * o cualquier instancia de Redis usando las mismas credenciales
     * que Spring Data Redis.
     */
    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        Config config = new Config();

        String address = String.format("redis://%s:%d", redisHost, redisPort);

        config.useSingleServer()
                .setAddress(address)
                .setPassword(redisPassword != null && !redisPassword.isEmpty() ? redisPassword : null)
                .setConnectionMinimumIdleSize(minIdle)
                .setConnectionPoolSize(maxActive)
                .setSubscriptionConnectionPoolSize(2)
                .setConnectTimeout(timeout * 2)
                .setTimeout(timeout)
                .setRetryAttempts(3)
                .setClientName("hibernate-cache");

        return Redisson.create(config);
    }
}
