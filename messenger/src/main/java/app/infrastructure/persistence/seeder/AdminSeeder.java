package app.infrastructure.persistence.seeder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import app.domain.model.enums.Role;
import app.infrastructure.persistence.entities.EmployeeEntity;
import app.infrastructure.persistence.repository.EmployeeRepository;

/**
 * Seeder que crea el usuario administrador inicial al arrancar la aplicación.
 */
// @Component
public class AdminSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(AdminSeeder.class);

    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (employeeRepository.count() == 0) {
            EmployeeEntity admin = new EmployeeEntity();
            admin.setDocument(1000000000L);
            admin.setFullName("Administrador");
            admin.setPhone("3000000000");
            admin.setPassword(passwordEncoder.encode("Admin123!"));
            admin.setRole(Role.ADMIN);

            employeeRepository.save(admin);

            logger.info("=============================================");
            logger.info("🚀 Usuario ADMIN creado exitosamente");
            logger.info("👤 Documento: 1000000000");
            logger.info("🔑 Pass: Admin123!");
            logger.info("=============================================");
        }
    }
}
