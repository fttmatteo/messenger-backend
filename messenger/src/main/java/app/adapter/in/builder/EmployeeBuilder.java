package app.adapter.in.builder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import app.adapter.in.validators.EmployeeValidator;
import app.domain.model.Employee;

/**
 * Builder para construir objetos Employee validados desde DTOs.
 * 
 * <p>
 * Valida y construye instancias de Employee aplicando reglas de negocio.
 * </p>
 */
@Component
public class EmployeeBuilder {

    @Autowired
    private EmployeeValidator validator;

    public Employee build(String document, String fullName, String phone, String userName, String password, String role)
            throws Exception {
        Employee employee = new Employee();
        employee.setDocument(validator.documentValidator(document));
        employee.setFullName(validator.fullNameValidator(fullName));
        employee.setPhone(validator.phoneValidator(phone));
        employee.setUserName(validator.userNameValidator(userName));
        employee.setPassword(validator.passwordValidator(password));
        employee.setRole(validator.roleValidator(role));
        return employee;
    }
}