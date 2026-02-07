package app.infrastructure.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.ConstantDelay;
import java.time.Duration;
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
                .setConnectionMinimumIdleSize(2)
                .setConnectionPoolSize(8)
                .setConnectTimeout(10000)
                .setTimeout(5000)
                .setRetryAttempts(3)
                .setRetryDelay(new ConstantDelay(Duration.ofMillis(1500)))
                .setClientName("hibernate-cache");

        return Redisson.create(config);
    }
}
