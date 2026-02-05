package app.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * Configuración de caché para tests.
 * Usa ConcurrentMapCacheManager en lugar de Redis para evitar
 * dependencia de infraestructura externa durante los tests.
 */
@TestConfiguration
public class TestCacheConfig {

    @Bean
    @Primary
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(
                "dealerships",
                "employees",
                "services",
                "service-details",
                "plates");
    }
}
