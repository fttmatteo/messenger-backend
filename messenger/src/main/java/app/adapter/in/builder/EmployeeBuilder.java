package app.adapter.in.builder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import app.adapter.in.validators.EmployeeValidator;
import app.domain.model.Employee;

@Component
public class EmployeeBuilder {

    @Autowired
    private EmployeeValidator validator;
    
    public Employee build( String document, String full_name, String phone, String user_name, String password, String zone) throws Exception {
        Employee employee = new Employee();
        employee.setDocument(validator.documentValidator(document));
        employee.setFull_name(validator.fullNameValidator(full_name));
        employee.setPhone(validator.phoneValidator(phone));
        employee.setUser_name(validator.userNameValidator(user_name));
        employee.setPassword(validator.passwordValidator(password));
        employee.setZone(validator.zoneValidator(zone));
        return employee;
    }
}