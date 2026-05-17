package app.adapter.out.tracking.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import java.util.concurrent.Executor;

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

        container.setTaskExecutor(redisPubSubExecutor());

        return container;
    }

    @Bean
    public Executor redisPubSubExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("redis-sub-");
        executor.initialize();
        return executor;
    }

    @Bean
    public MessageListenerAdapter listenerAdapter(RedisTrackingSubscriber subscriber) {
        return new MessageListenerAdapter(subscriber, "onMessage");
    }
}
