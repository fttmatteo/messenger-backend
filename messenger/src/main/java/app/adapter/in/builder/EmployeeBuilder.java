package app.adapter.in.builder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import app.adapter.in.validators.EmployeeValidator;
import app.domain.model.Employee;

/**
 * Componente encargado de la construcción de objetos {@link Employee}.
 *
 * Aplica validaciones de reglas de negocio a través de
 * {@link EmployeeValidator}
 * antes de crear la instancia del modelo de dominio.
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