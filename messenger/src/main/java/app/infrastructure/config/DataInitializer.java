package app.infrastructure.config;

import app.domain.model.enums.Role;
import app.infrastructure.persistence.entities.EmployeeEntity;
import app.infrastructure.persistence.repository.EmployeeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Inicializador de datos para el perfil 'local'.
 * Asegura que existan usuarios de prueba para una experiencia Zero-Config.
 */
@Configuration
@Profile("local")
public class DataInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    public CommandLineRunner initData(EmployeeRepository employeeRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            logger.info("Iniciando carga de datos Zero-Config para perfil local...");

            // 1. Crear Administrador (123456 / admin123)
            if (employeeRepository.findByDocument(123456L) == null) {
                EmployeeEntity admin = new EmployeeEntity();
                admin.setDocument(123456L);
                admin.setFullName("Administrador de Pruebas");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole(Role.ADMIN);
                admin.setPhone("3000000001");
                employeeRepository.save(admin);
                logger.info("Usuario Administrador creado: 123456 / admin123");
            }

            // 2. Crear Mensajero (654321 / password123)
            if (employeeRepository.findByDocument(654321L) == null) {
                EmployeeEntity messenger = new EmployeeEntity();
                messenger.setDocument(654321L);
                messenger.setFullName("Mensajero de Pruebas");
                messenger.setPassword(passwordEncoder.encode("password123"));
                messenger.setRole(Role.MESSENGER);
                messenger.setPhone("3000000002");
                employeeRepository.save(messenger);
                logger.info("Usuario Mensajero creado: 654321 / password123");
            }

            logger.info("Carga de datos Zero-Config completada.");
        };
    }
}
