package app.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuración para procesamiento asíncrono.
 * Utilizado para el envío de notificaciones de WhatsApp sin bloquear el flujo principal.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "whatsappTaskExecutor")
    public Executor whatsappTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("WhatsApp-Async-");
        executor.initialize();
        return executor;
    }
}
