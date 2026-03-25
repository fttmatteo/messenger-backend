package app.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuración para la ejecución asíncrona de tareas (@Async).
 * Reemplaza el SimpleAsyncTaskExecutor por defecto con un pool de hilos controlado.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // Configuración optimizada para un entorno de 1GiB RAM
        executor.setCorePoolSize(5);        // Hilos mínimos siempre activos
        executor.setMaxPoolSize(20);       // Máximo de hilos bajo carga
        executor.setQueueCapacity(100);    // Cola de espera antes de crear más hilos
        executor.setThreadNamePrefix("AsyncThread-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }
}
