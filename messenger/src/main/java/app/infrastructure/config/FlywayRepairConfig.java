package app.infrastructure.config;

import org.springframework.boot.flyway.autoconfigure.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración para solucionar problemas comunes de Flyway.
 * Ejecuta 'repair' antes de cada migración para limpiar estados fallidos.
 */
@Configuration
public class FlywayRepairConfig {

    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return flyway -> {
            // Repair limpia la tabla flyway_schema_history de entradas fallidas
            // Esto es útil cuando una migración falló a mitad de camino y bloquea el
            // reinicio.
            flyway.repair();
            // Ejecuta las migraciones pendientes
            flyway.migrate();
        };
    }
}
