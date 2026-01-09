package app.adapter.out.tracking.config;

import app.domain.model.LiveTracking;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Configuración de Redis para tracking en tiempo real.
 * Usa serialización JSON con tolerancia a cambios de schema:
 * - FAIL_ON_UNKNOWN_PROPERTIES deshabilitado para compatibilidad hacia adelante
 * - Si se agregan nuevos campos, versiones antiguas los ignorarán
 * - Si se quitan campos, se deserializarán como null
 */
@Configuration
@ConditionalOnProperty(name = "redis.enabled", havingValue = "true", matchIfMissing = true)
public class RedisConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;
    @Value("${spring.data.redis.port:6379}")
    private int redisPort;
    @Value("${spring.data.redis.password:}")
    private String redisPassword;

    /**
     * Crea la factoría de conexiones a Redis (Lettuce).
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisHost);
        config.setPort(redisPort);

        if (redisPassword != null && !redisPassword.isEmpty()) {
            config.setPassword(redisPassword);
        }

        return new LettuceConnectionFactory(config);
    }

    /**
     * Configura el template de Redis para manejar objetos LiveTracking con
     * serialización JSON compatible con cambios de versión.
     */
    @Bean
    public RedisTemplate<String, LiveTracking> liveTrackingRedisTemplate(
            RedisConnectionFactory connectionFactory) {

        RedisTemplate<String, LiveTracking> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // Configurar ObjectMapper tolerante a cambios de schema
        ObjectMapper objectMapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                // No fallar si hay campos desconocidos (compatibilidad hacia adelante)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build();

        Jackson2JsonRedisSerializer<LiveTracking> serializer = new Jackson2JsonRedisSerializer<>(objectMapper,
                LiveTracking.class);

        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }

}
