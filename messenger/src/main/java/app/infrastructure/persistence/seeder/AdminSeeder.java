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
 * Seeder para inicializar la base de datos con un usuario administrador por
 * defecto.
 * Se ejecuta al iniciar la aplicación.
 */
@Component
public class AdminSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(AdminSeeder.class);

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Verificar si ya existen empleados para evitar duplicados
        if (employeeRepository.count() == 0) {
            EmployeeEntity admin = new EmployeeEntity();
            admin.setDocument(1000000000L);
            admin.setFullName("Administrador");
            admin.setPhone("3000000000");
            admin.setUserName("adm");
            admin.setPassword(passwordEncoder.encode("adm"));
            admin.setRole(Role.ADMIN);

            employeeRepository.save(admin);

            logger.info("=============================================");
            logger.info("🚀 SEEDER: Usuario ADMIN creado exitosamente");
            logger.info("👤 User: adm");
            logger.info("🔑 Pass: adm");
            logger.info("=============================================");
        }
    }
}
