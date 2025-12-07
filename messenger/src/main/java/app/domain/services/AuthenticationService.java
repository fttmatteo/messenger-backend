package app.domain.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import app.adapter.in.validators.AuthValidator;
import app.domain.model.Employee;
import app.domain.model.auth.AuthCredentials;
import app.domain.model.auth.TokenResponse;
import app.domain.ports.AuthenticationPort;
import app.domain.ports.EmployeePort;

@Service
public class AuthenticationService {

    @Autowired
    private AuthenticationPort authenticationPort;
    @Autowired
    private EmployeePort employeePort;
    @Autowired
    private AuthValidator validator;

    public TokenResponse authenticate(AuthCredentials credentials) throws Exception {
        Employee employee = employeePort.findByUserName(credentials.getUserName());
        validator.validateLogin(employee, credentials.getPassword());
        return authenticationPort.authenticate(credentials, employee.getRole().name());
    }
}