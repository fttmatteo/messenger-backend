package app.adapter.in.builder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import app.adapter.in.validators.EmployeeValidator;
import app.domain.model.Employee;
import app.domain.model.enums.Role;

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
        employee.setRole(validateRole(role));
        return employee;
    }

    private Role validateRole(String roleStr) throws Exception {
        if (roleStr == null || roleStr.trim().isEmpty()) {
            throw new Exception("El rol es obligatorio");
        }

        try {
            return Role.valueOf(roleStr.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new Exception("Rol inválido. Debe ser ADMIN o MESSENGER");
        }
    }
}