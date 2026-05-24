package app.application.usecase.employee;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import app.domain.exception.BusinessException;
import app.domain.model.Employee;
import app.domain.ports.EmployeePort;
import app.domain.util.LogSanitizer;

/**
 * Servicio para crear nuevos empleados con encriptación de contraseña.
 */
@Service
public class CreateEmployee {

    private static final Logger logger = LoggerFactory.getLogger(CreateEmployee.class);

    @Autowired
    private EmployeePort employeePort;
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Registra un nuevo empleado, validando duplicidad de documento y encriptando
     * contraseña.
     */
    public Employee create(Employee employee) throws Exception {
        validateDocumentIsUnique(employee.getDocument());

        if (employee.getPassword() != null) {
            String encoded = passwordEncoder.encode(employee.getPassword());
            employee.setPassword(encoded);
        }

        Employee saved = employeePort.save(employee);
        logger.info("Empleado creado exitosamente.");
        return saved;
    }

    private void validateDocumentIsUnique(Long document) throws Exception {
        if (employeePort.findByDocument(document) != null) {
            logger.warn("Intento de crear empleado con documento duplicado: {}", LogSanitizer.maskDocument(document));
            throw new BusinessException("Ya existe un empleado registrado con ese documento.");
        }
    }
}