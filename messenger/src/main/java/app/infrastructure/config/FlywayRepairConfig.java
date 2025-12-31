package app.infrastructure.config;

import org.springframework.boot.flyway.autoconfigure.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.context.annotation.Profile;

/**
 * Configuración para solucionar problemas comunes de Flyway.
 * Ejecuta 'repair' antes de cada migración para limpiar estados fallidos.
 */
@Configuration
@Profile("prod")
public class FlywayRepairConfig {

    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return flyway -> {
            flyway.repair();
            flyway.migrate();
        };
    }
}
