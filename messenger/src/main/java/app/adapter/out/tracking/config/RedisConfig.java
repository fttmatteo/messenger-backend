package app.adapter.out.tracking.config;

import app.domain.model.LiveTracking;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Configuración de Redis para tracking de mensajeros en tiempo real.
 * 
 * Esta clase configura la conexión y serialización de Redis, optimizado
 * para almacenar ubicaciones de mensajeros con expiración automática.
 * 
 * Configuración de conexión:
 * - Host: spring.data.redis.host (default: localhost)
 * - Puerto: spring.data.redis.port (default: 6379)
 * - Cliente: Lettuce (asíncrono y thread-safe)
 * 
 * Configuración de serialización:
 * - Keys: StringRedisSerializer (formato texto simple)
 * - Values: RedisSerializer.json() (JSON type-safe, recomendado Spring Data
 * Redis 4.0+)
 * 
 * Ventajas de Redis para tracking:
 * - Acceso ultra-rápido (sub-milisegundo)
 * - TTL automático para limpiar datos antiguos
 * - Soporte nativo para operaciones atómicas
 * - Bajo consumo de memoria con expiración
 * 
 * @see TrackingAdapter
 * @see app.domain.model.LiveTracking
 */
@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    /**
     * Crea la fábrica de conexiones a Redis usando Lettuce.
     * 
     * Lettuce es el cliente recomendado por Spring Boot por ser asíncrono
     * y thread-safe, ideal para aplicaciones de alto rendimiento.
     * 
     * @return Fábrica de conexiones configurada
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisHost);
        config.setPort(redisPort);
        return new LettuceConnectionFactory(config);
    }

    /**
     * Crea el RedisTemplate configurado para objetos LiveTracking.
     * 
     * Configura serializadores apropiados:
     * - Keys: String (para búsquedas eficientes)
     * - Values: JSON (para objetos LiveTracking complejos)
     * 
     * Usa RedisSerializer.json() que es type-safe y no genera warnings de
     * deprecación.
     * 
     * @param connectionFactory Fábrica de conexiones
     * @return RedisTemplate configurado para LiveTracking
     */
    @Bean
    public RedisTemplate<String, LiveTracking> liveTrackingRedisTemplate(
            RedisConnectionFactory connectionFactory) {

        RedisTemplate<String, LiveTracking> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Serializer para keys (String)
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // Configurar Jackson 3 usando com.fasterxml.jackson
        com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // Usar Jackson2JsonRedisSerializer con tipo específico
        org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer<LiveTracking> serializer = new org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer<>(
                LiveTracking.class);

        serializer.setObjectMapper(objectMapper);

        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }

}
