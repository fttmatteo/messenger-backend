package app.infrastructure.config;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.redis.spring.RedisLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * Configuración de ShedLock para asegurar que las tareas programadas
 * (@Scheduled)
 * solo se ejecuten en una instancia a la vez en un entorno distribuido.
 */
@Configuration
@EnableSchedulerLock(defaultLockAtMostFor = "PT30M")
public class ShedLockConfig {

    @Bean
    public LockProvider lockProvider(RedisConnectionFactory connectionFactory) {
        // "shedlock" es el nombre del espacio de nombres en Redis para los locks
        return new RedisLockProvider(connectionFactory, "shedlock");
    }
}
