package app.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

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
