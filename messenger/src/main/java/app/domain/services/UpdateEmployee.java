package app.domain.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import app.application.exceptions.BusinessException;
import app.domain.model.Employee;
import app.domain.ports.EmployeePort;

/**
 * Servicio para actualizar datos de empleados.
 */
@Service
public class UpdateEmployee {

    @Autowired
    private EmployeePort employeePort;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public Employee update(Long id, Employee incomingData) throws Exception {
        Employee existingEmployee = employeePort.findById(id);
        if (existingEmployee == null) {
            throw new BusinessException("El empleado con ID " + id + " no existe.");
        }

        if (!existingEmployee.getDocument().equals(incomingData.getDocument())) {
            if (employeePort.findByDocument(incomingData.getDocument()) != null) {
                throw new BusinessException(
                        "El documento " + incomingData.getDocument() + " ya está registrado por otro empleado.");
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