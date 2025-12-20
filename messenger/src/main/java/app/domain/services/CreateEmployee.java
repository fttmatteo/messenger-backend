package app.domain.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import app.application.exceptions.BusinessException;
import app.domain.model.Employee;
import app.domain.ports.EmployeePort;

/**
 * Servicio de dominio para crear nuevos empleados/mensajeros.
 * 
 * Gestiona la creación de empleados validando:
 * Unicidad del número de documento
 * Unicidad del nombre de usuario
 * Encriptación automática de contraseñas con BCrypt
 */
@Service
public class CreateEmployee {

    private static final Logger logger = LoggerFactory.getLogger(CreateEmployee.class);

    @Autowired
    private EmployeePort employeePort;
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Crea un nuevo empleado en el sistema.
     * 
     * Valida unicidad de documento y username, y encripta la contraseña con BCrypt.
     * 
     * @param employee Empleado a crear.
     * @return El empleado creado con su ID asignado.
     * @throws Exception Si el documento o username ya están en uso.
     */
    public Employee create(Employee employee) throws Exception {
        logger.debug("Creando empleado: {}", employee.getUserName());
        validateDocumentIsUnique(employee.getDocument());
        validateUserNameIsUnique(employee.getUserName());

        if (employee.getPassword() != null) {
            String encoded = passwordEncoder.encode(employee.getPassword());
            employee.setPassword(encoded);
        }

        Employee saved = employeePort.save(employee);
        logger.info("Empleado creado exitosamente: {} (doc: {})", saved.getUserName(), saved.getDocument());
        return saved;
    }

    private void validateDocumentIsUnique(Long document) throws Exception {
        if (employeePort.existsByDocument(document)) {
            throw new BusinessException("Ya existe un empleado registrado con el documento " + document);
        }
    }

    private void validateUserNameIsUnique(String userName) throws Exception {
        Employee existing = employeePort.findByUserName(userName);
        if (existing != null) {
            throw new BusinessException("El nombre de usuario " + userName + " ya está en uso.");
        }
    }
}