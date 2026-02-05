package app.infrastructure.config;

import java.time.Duration;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Configuración de caché con Redis para optimizar rendimiento.
 * 
 * Estrategia de caché:
 * - dealerships: 30 minutos (datos que cambian poco)
 * - employees: 15 minutos (datos que cambian poco)
 * - services: 5 minutos (datos que cambian frecuentemente)
 * - service-details: 2 minutos (detalles individuales)
 */
@Configuration
@Profile("!test")
public class CacheConfig implements CachingConfigurer {

        @Bean
        public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
                // ObjectMapper configurado para manejar LocalDateTime y otros tipos Java 8
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.registerModule(new JavaTimeModule());
                objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                objectMapper.activateDefaultTyping(
                                objectMapper.getPolymorphicTypeValidator(),
                                ObjectMapper.DefaultTyping.NON_FINAL);

                GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(
                                objectMapper);

                // Configuración por defecto: TTL de 10 minutos
                RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofMinutes(10))
                                .serializeKeysWith(RedisSerializationContext.SerializationPair
                                                .fromSerializer(new StringRedisSerializer()))
                                .serializeValuesWith(RedisSerializationContext.SerializationPair
                                                .fromSerializer(jsonSerializer))
                                .disableCachingNullValues();

                return RedisCacheManager.builder(connectionFactory)
                                .cacheDefaults(defaultConfig)
                                // Configuraciones específicas por caché
                                .withCacheConfiguration("dealerships",
                                                defaultConfig.entryTtl(Duration.ofMinutes(30)))
                                .withCacheConfiguration("employees",
                                                defaultConfig.entryTtl(Duration.ofMinutes(15)))
                                .withCacheConfiguration("services",
                                                defaultConfig.entryTtl(Duration.ofMinutes(5)))
                                .withCacheConfiguration("service-details",
                                                defaultConfig.entryTtl(Duration.ofMinutes(2)))
                                .transactionAware()
                                .build();
        }

        /**
         * Generador de claves personalizado para métodos con múltiples parámetros.
         * Genera claves únicas basadas en el nombre de la clase, método y parámetros.
         */
        @Override
        @Bean
        public KeyGenerator keyGenerator() {
                return (target, method, params) -> {
                        StringBuilder sb = new StringBuilder();
                        sb.append(target.getClass().getSimpleName());
                        sb.append(".");
                        sb.append(method.getName());
                        for (Object param : params) {
                                sb.append("_");
                                if (param != null) {
                                        sb.append(param.toString());
                                } else {
                                        sb.append("null");
                                }
                        }
                        return sb.toString();
                };
        }
}
