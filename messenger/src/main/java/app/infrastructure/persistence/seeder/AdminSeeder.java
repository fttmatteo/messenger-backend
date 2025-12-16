package app.infrastructure.persistence.seeder;

import app.domain.model.enums.Role;
import app.infrastructure.persistence.entities.EmployeeEntity;
import app.infrastructure.persistence.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeder para inicializar la base de datos con un usuario administrador por
 * defecto.
 * Se ejecuta al iniciar la aplicación.
 * 
 * Si la tabla de empleados está vacía, crea un usuario:
 * - Username: admin
 * - Password: admin123
 * - Role: ADMIN
 */
@Component
public class AdminSeeder implements CommandLineRunner {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Verificar si ya existen empleados para evitar duplicados o sobrescritura
        if (employeeRepository.count() == 0) {
            EmployeeEntity admin = new EmployeeEntity();
            // Documento ficticio pero válido
            admin.setDocument(1000000000L);
            admin.setFullName("Administrador del Sistema");
            admin.setPhone("3000000000");
            admin.setUserName("admin");
            // Encriptamos la contraseña con la misma estrategia que usa el login (BCrypt)
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ADMIN);

            employeeRepository.save(admin);

            System.out.println("=============================================");
            System.out.println("🚀 SEEDER: Usuario ADMIN creado exitosamente");
            System.out.println("👤 User: admin");
            System.out.println("🔑 Pass: admin123");
            System.out.println("=============================================");
        }
    }
}
