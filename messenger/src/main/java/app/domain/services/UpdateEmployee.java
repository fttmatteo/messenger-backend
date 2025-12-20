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
 * Servicio de dominio para actualizar información de empleados existentes.
 * 
 * Permite actualizar datos del empleado validando:
 * 
 * Existencia del empleado a actualizar
 * Unicidad de documento si se modifica
 * Unicidad de username si se modifica
 * Re-encriptación de contraseña si se proporciona nueva
 */
@Service
public class UpdateEmployee {

    private static final Logger logger = LoggerFactory.getLogger(UpdateEmployee.class);

    @Autowired
    private EmployeePort employeePort;
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Actualiza la información de un empleado existente.
     * 
     * Valida unicidad de documento y username si se modifican, y re-encripta la
     * contraseña si se proporciona.
     * 
     * @param id           ID del empleado a actualizar.
     * @param incomingData Nuevos datos del empleado.
     * @return El empleado actualizado.
     * @throws Exception Si el empleado no existe, o el documento/username ya están
     *                   en uso.
     */
    public Employee update(Long id, Employee incomingData) throws Exception {
        logger.debug("Actualizando empleado ID: {}", id);
        Employee existingEmployee = employeePort.findById(id);
        if (existingEmployee == null) {
            throw new BusinessException("El empleado con ID " + id + " no existe.");
        }

        if (!existingEmployee.getDocument().equals(incomingData.getDocument())) {
            Employee other = employeePort.findByDocument(incomingData.getDocument());
            if (other != null) {
                throw new BusinessException(
                        "El documento " + incomingData.getDocument() + " ya está registrado por otro empleado.");
            }
            existingEmployee.setDocument(incomingData.getDocument());
        }

        if (!existingEmployee.getUserName().equals(incomingData.getUserName())) {
            Employee other = employeePort.findByUserName(incomingData.getUserName());
            if (other != null) {
                throw new BusinessException("El nombre de usuario " + incomingData.getUserName() + " ya está en uso.");
            }
            existingEmployee.setUserName(incomingData.getUserName());
        }

        existingEmployee.setFullName(incomingData.getFullName());
        existingEmployee.setPhone(incomingData.getPhone());
        existingEmployee.setRole(incomingData.getRole());

        if (incomingData.getPassword() != null && !incomingData.getPassword().trim().isEmpty()) {
            String encoded = passwordEncoder.encode(incomingData.getPassword());
            existingEmployee.setPassword(encoded);
            logger.debug("Contraseña actualizada para empleado ID: {}", id);
        }

        Employee updated = employeePort.save(existingEmployee);
        logger.info("Empleado actualizado: ID {} -> {}", id, updated.getUserName());
        return updated;
    }
}