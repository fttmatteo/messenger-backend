package app.adapter.out.tracking.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

/**
 * Configuración de Redis Pub/Sub para coordinar eventos WebSocket entre
 * instancias.
 */
@Configuration
public class RedisPubSubConfig {

    public static final String TRACKING_TOPIC = "tracking:updates";

    @Bean
    public RedisMessageListenerContainer redisContainer(
            RedisConnectionFactory connectionFactory,
            MessageListenerAdapter listenerAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(listenerAdapter, new ChannelTopic(TRACKING_TOPIC));
        return container;
    }

    @Bean
    public MessageListenerAdapter listenerAdapter(RedisTrackingSubscriber subscriber) {
        // "onMessage" es el método que se llamará en el suscriptor
        return new MessageListenerAdapter(subscriber, "onMessage");
    }
}
