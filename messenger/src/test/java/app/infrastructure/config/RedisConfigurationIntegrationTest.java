package app.infrastructure.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.domain.model.LiveTracking;
import app.domain.model.Location;
import app.domain.model.enums.TrackingStatus;
import app.support.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.LocalDateTime;

/**
 * Pruebas de integración para verificar la correcta configuración de Redis:
 * - Pooling de conexiones (Lettuce y Redisson).
 * - Sincronización de propiedades (max-active=4).
 * - Funcionalidad básica con Testcontainers.
 */
@DisplayName("Redis Configuration Integration Tests")
class RedisConfigurationIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private RedisConnectionFactory connectionFactory;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private RedisTemplate<String, LiveTracking> liveTrackingTemplate;

    @Test
    @DisplayName("Should verify Lettuce connection factory is correctly pooled")
    void shouldVerifyLettucePoolingConfiguration() {
        assertTrue(connectionFactory instanceof LettuceConnectionFactory, 
            "Debe ser una instancia de LettuceConnectionFactory");
        
        LettuceConnectionFactory lettuceFactory = (LettuceConnectionFactory) connectionFactory;
        
        assertTrue(lettuceFactory.getClientConfiguration() instanceof LettucePoolingClientConfiguration,
            "Debe tener habilitado el pooling (LettucePoolingClientConfiguration)");
        
        LettucePoolingClientConfiguration poolConfig = (LettucePoolingClientConfiguration) lettuceFactory.getClientConfiguration();
        
        // Verificar que los límites coincidan con application.properties (Estandarización Professional)
        assertEquals(4, poolConfig.getPoolConfig().getMaxTotal(), "Max active connections debe ser 4");
        assertEquals(2, poolConfig.getPoolConfig().getMaxIdle(), "Max idle connections debe ser 2");
        assertEquals(1, poolConfig.getPoolConfig().getMinIdle(), "Min idle connections debe ser 1");
    }

    @Test
    @DisplayName("Should verify Redisson is synchronized with shared pool properties")
    void shouldVerifyRedissonConfigurationSync() {
        Config config = redissonClient.getConfig();
        assertNotNull(config, "Configuración de Redisson no debe ser nula");
        
        // Redisson usa SingleServerConfig en este proyecto
        assertEquals(4, config.useSingleServer().getConnectionPoolSize(), 
            "Redisson debe usar el mismo pool size de 4 conexiones");
        assertEquals(1, config.useSingleServer().getConnectionMinimumIdleSize(), 
            "Redisson debe usar el mismo min idle de 1");
        assertEquals(10000, config.useSingleServer().getConnectTimeout(),
            "Connect timeout debe ser el doble del timeout base (5s * 2)");
    }

    @Test
    @DisplayName("Should perform basic operations successfully using connection pool")
    void shouldPerformBasicRedisOperations() {
        String key = "test:tracking:123";
        LiveTracking tracking = new LiveTracking();
        tracking.setMessengerId(123L);
        tracking.setStatus(TrackingStatus.ACTIVE);
        tracking.setCurrentLocation(new Location(4.6097, -74.0817, LocalDateTime.now(), 5.0));

        // Escribir
        liveTrackingTemplate.opsForValue().set(key, tracking);

        // Leer
        LiveTracking result = liveTrackingTemplate.opsForValue().get(key);

        assertNotNull(result);
        assertEquals(123L, result.getMessengerId());
        assertEquals(TrackingStatus.ACTIVE, result.getStatus());
        assertEquals(4.6097, result.getCurrentLocation().getLatitude());
    }
}
