package app.domain.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import app.application.exceptions.BusinessException;
import app.domain.model.Employee;
import app.domain.ports.EmployeePort;

@Service
public class CreateEmployee {

    @Autowired
    private EmployeePort employeePort;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public Employee create(Employee employee) throws Exception {
        validateDocumentIsUnique(employee.getDocument());
        validateUserNameIsUnique(employee.getUserName());

        if (employee.getPassword() != null) {
            String encoded = passwordEncoder.encode(employee.getPassword());
            employee.setPassword(encoded);
        }

        Employee saved = employeePort.save(employee);
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