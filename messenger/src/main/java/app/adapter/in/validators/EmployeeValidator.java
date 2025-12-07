package app.adapter.in.validators;

import java.util.List;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import app.application.exceptions.BusinessException;
import app.application.exceptions.InputsException;
import app.domain.model.Employee;
import app.domain.model.ServiceDelivery;
import app.domain.ports.EmployeePort;
import app.domain.ports.ServiceDeliveryPort;

@Component
public class EmployeeValidator extends SimpleValidator {

    @Autowired
    private EmployeePort employeePort;

    @Autowired
    private ServiceDeliveryPort serviceDeliveryPort;

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z0-9]+$");

    // --- Validaciones de Formato (Inputs) ---

    public String fullNameValidator(String value) throws InputsException {
        return stringValidator("nombre completo", value);
    }

    public long documentValidator(String value) throws InputsException {
        long doc = longValidator("número de cédula", value);
        if (String.valueOf(Math.abs(doc)).length() > 10) {
            throw new InputsException("la cédula no puede exceder 10 dígitos");
        }
        return doc;
    }

    public String phoneValidator(String value) throws InputsException {
        stringValidator("número de teléfono", value);
        if (!value.matches("\\d{10}")) {
            throw new InputsException("el número de teléfono debe contener 10 dígitos");
        }
        return value;
    }

    public String userNameValidator(String value) throws InputsException {
        stringValidator("nombre de usuario", value);
        if (value.length() > 15) {
            throw new InputsException("el nombre de usuario no puede exceder 15 caracteres");
        }
        if (!USERNAME_PATTERN.matcher(value).matches()) {
            throw new InputsException("el nombre de usuario solo debe contener letras y números");
        }
        return value;
    }

    public String passwordValidator(String value) throws InputsException {
        stringValidator("contraseña", value);
        if (value.length() < 8) {
            throw new InputsException("la contraseña debe contener al menos 8 caracteres");
        }
        // Puedes agregar más reglas de complejidad aquí si lo deseas
        return value;
    }

    // --- Validaciones de Reglas de Negocio (Business) ---

    public void validateCreation(Employee employee) throws Exception {
        if (employeePort.findByDocument(employee.getDocument()) != null) {
            throw new BusinessException("Ya existe un empleado registrado con esa cédula: " + employee.getDocument());
        }
        if (employeePort.findByUserName(employee.getUserName()) != null) {
            throw new BusinessException("El nombre de usuario '" + employee.getUserName() + "' ya está en uso.");
        }
    }

    public void validateUpdate(Employee newEmployee, Employee existingEmployee) throws Exception {
        // Validar cambio de cédula
        if (!existingEmployee.getDocument().equals(newEmployee.getDocument())) {
            if (employeePort.findByDocument(newEmployee.getDocument()) != null) {
                throw new BusinessException("La nueva cédula ya pertenece a otro empleado.");
            }
            // Validar integridad referencial si cambia su ID principal de negocio
            validateNoActiveDeliveries(existingEmployee.getDocument());
        }

        // Validar cambio de usuario
        if (!existingEmployee.getUserName().equals(newEmployee.getUserName())) {
            if (employeePort.findByUserName(newEmployee.getUserName()) != null) {
                throw new BusinessException("El nombre de usuario '" + newEmployee.getUserName() + "' ya está en uso.");
            }
        }
    }

    public void validateDelete(Long document) throws BusinessException {
        validateNoActiveDeliveries(document);
    }

    private void validateNoActiveDeliveries(Long messengerDocument) throws BusinessException {
        List<ServiceDelivery> deliveries = serviceDeliveryPort.findByMessengerDocument(messengerDocument);
        if (deliveries != null && !deliveries.isEmpty()) {
            throw new BusinessException("NO SE PUEDE EJECUTAR LA ACCIÓN: El empleado tiene " + deliveries.size() + 
                " servicios de entrega asociados. Reasigne los servicios o elimínelos primero.");
        }
    }
}