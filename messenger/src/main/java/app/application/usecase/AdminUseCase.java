package app.application.usecase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import app.domain.model.Employee;
import app.domain.model.enums.Role;
import app.domain.services.CreateEmployee;
import app.domain.services.DeleteEmployee;

@Service
public class AdminUseCase {

    @Autowired
    private CreateEmployee createEmployee;

    @Autowired
    private DeleteEmployee deleteEmployee;

    public void createMessenger(Employee employee) throws Exception {
        employee.setRole(Role.MESSENGER);
        createEmployee.create(employee);
    }

    public void documentIsUnique(Long document) throws Exception {
        createEmployee.documentIsUnique(document);
    }

    public void userNameIsUnique(String userName) throws Exception {
        createEmployee.userNameIsUnique(userName);
    }

    public void deleteEmployee(Long document) throws Exception {
        deleteEmployee.deleteByDocument(document);
    }
}