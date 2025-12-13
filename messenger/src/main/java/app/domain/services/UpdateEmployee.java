package app.domain.services;

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

    @Autowired
    private EmployeePort employeePort;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public void update(Long id, Employee incomingData) throws Exception {
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
        }

        employeePort.save(existingEmployee);
    }
}