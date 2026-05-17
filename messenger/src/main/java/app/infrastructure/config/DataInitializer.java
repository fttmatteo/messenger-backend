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
 * Inicializador de datos para los perfiles 'local' y 'dev'.
 * Asegura que existan usuarios de prueba para una experiencia Zero-Config.
 */
@Configuration
@Profile({ "local", "dev" })
public class DataInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    public CommandLineRunner initData(EmployeeRepository employeeRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            logger.info("Iniciando carga de datos para perfil local y dev...");

            if (employeeRepository.findByDocument(123456L) == null) {
                EmployeeEntity admin = new EmployeeEntity();
                admin.setDocument(123456L);
                admin.setFullName("Administrador de Pruebas");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole(Role.ADMIN);
                admin.setPhone("3000000001");
                employeeRepository.save(admin);
            }

            if (employeeRepository.findByDocument(654321L) == null) {
                EmployeeEntity messenger = new EmployeeEntity();
                messenger.setDocument(654321L);
                messenger.setFullName("Mensajero de Pruebas");
                messenger.setPassword(passwordEncoder.encode("password123"));
                messenger.setRole(Role.MESSENGER);
                messenger.setPhone("3000000002");
                employeeRepository.save(messenger);
            }

            logger.info("Carga de datos Zero-Config completada.");
        };
    }
}
