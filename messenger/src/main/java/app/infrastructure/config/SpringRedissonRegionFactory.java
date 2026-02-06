package app.infrastructure.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.hibernate.RedissonRegionFactory;
import org.redisson.config.ConstantDelay;
import org.hibernate.boot.spi.SessionFactoryOptions;

import java.time.Duration;
import java.lang.reflect.Field;
import java.util.Map;

/**
 * RegionFactory personalizada para Hibernate L2 Cache que lee la configuración
 * de Redis desde las propiedades de Hibernate (que vienen de Spring
 * properties).
 * 
 * Esto permite usar las mismas variables de entorno que Spring Data Redis
 * sin necesidad de un archivo YAML separado.
 */
@SuppressWarnings({ "rawtypes" })
public class SpringRedissonRegionFactory extends RedissonRegionFactory {

    private static final long serialVersionUID = 1L;

    @Override
    protected void prepareForUse(SessionFactoryOptions settings, Map properties) {
        // Leer propiedades de Hibernate que vienen de Spring application.properties
        String host = getProperty(properties, "hibernate.cache.redisson.host", "localhost");
        String portStr = getProperty(properties, "hibernate.cache.redisson.port", "6379");
        String password = getProperty(properties, "hibernate.cache.redisson.password", null);

        int port = Integer.parseInt(portStr);
        String address = String.format("redis://%s:%d", host, port);

        Config config = new Config();
        config.useSingleServer()
                .setAddress(address)
                .setPassword(password != null && !password.isEmpty() ? password : null)
                .setConnectionMinimumIdleSize(2)
                .setConnectionPoolSize(8)
                .setConnectTimeout(10000)
                .setTimeout(5000)
                .setRetryAttempts(3)
                .setRetryDelay(new ConstantDelay(Duration.ofMillis(1500)))
                .setClientName("hibernate-l2-cache");

        RedissonClient redissonClient = Redisson.create(config);

        // Usar reflexión para establecer el campo 'redisson' del padre
        try {
            Field redissonField = RedissonRegionFactory.class.getDeclaredField("redisson");
            redissonField.setAccessible(true);
            redissonField.set(this, redissonClient);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("No se pudo configurar el cliente Redisson para Hibernate L2 Cache", e);
        }

        // No llamamos super.prepareForUse() porque ya configuramos el cliente
    }

    private String getProperty(Map properties, String key, String defaultValue) {
        Object value = properties.get(key);
        if (value != null) {
            return value.toString();
        }
        return defaultValue;
    }
}
