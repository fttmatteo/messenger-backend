package app.adapter.in.builder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import app.adapter.in.validators.EmployeeValidator;
import app.domain.model.Employee;

@Component
public class EmployeeBuilder {

    @Autowired
    private EmployeeValidator validator;
    
    public Employee build( String document, String fullName, String phone, String userName, String password, String zone) throws Exception {
        Employee employee = new Employee();
        employee.setDocument(validator.documentValidator(document));
        employee.setFullName(validator.fullNameValidator(fullName));
        employee.setPhone(validator.phoneValidator(phone));
        employee.setUserName(validator.userNameValidator(userName));
        employee.setPassword(validator.passwordValidator(password));
        employee.setZone(validator.zoneValidator(zone));
        return employee;
    }
}