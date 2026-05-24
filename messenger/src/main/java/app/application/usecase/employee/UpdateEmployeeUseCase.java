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
 * Servicio para actualizar datos de empleados.
 */
@Service
public class UpdateEmployeeUseCase {

    private static final Logger logger = LoggerFactory.getLogger(UpdateEmployeeUseCase.class);

    @Autowired
    private EmployeePort employeePort;
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Actualiza la información de un empleado, controlando duplicidad de documento
     * y encriptación de password.
     */
    public Employee update(Long id, Employee incomingData) throws Exception {
        Employee existingEmployee = employeePort.findById(id);
        if (existingEmployee == null) {
            logger.warn("Intento de actualizar empleado inexistente.");
            throw new BusinessException("El empleado indicado no existe.");
        }

        if (!existingEmployee.getDocument().equals(incomingData.getDocument())) {
            if (employeePort.findByDocument(incomingData.getDocument()) != null) {
                logger.warn("Intento de actualizar empleado con documento duplicado: {}", LogSanitizer.maskDocument(incomingData.getDocument()));
                throw new BusinessException(
                        "Ese documento ya está registrado por otro empleado.");
            }
            existingEmployee.setDocument(incomingData.getDocument());
        }

        existingEmployee.setFullName(incomingData.getFullName());
        existingEmployee.setPhone(incomingData.getPhone());
        existingEmployee.setRole(incomingData.getRole());

        if (incomingData.getPassword() != null && !incomingData.getPassword().trim().isEmpty()) {
            String encoded = passwordEncoder.encode(incomingData.getPassword());
            existingEmployee.setPassword(encoded);
        }

        Employee updated = employeePort.save(existingEmployee);
        return updated;
    }
}