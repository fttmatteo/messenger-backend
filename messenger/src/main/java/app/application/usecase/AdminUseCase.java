package app.application.usecase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import app.domain.model.Dealership;
import app.domain.model.Employee;
import app.domain.model.Plate;
import app.domain.model.enums.Role;
import app.domain.ports.EmployeePort;
import app.domain.services.CreateDealership;
import app.domain.services.CreateEmployee;
import app.domain.services.DeleteEmployee;
import app.domain.services.DeleteDealership;
import app.domain.services.CreatePlate;
import app.domain.services.DeletePlate;
import app.domain.services.ManageService;

@Service
public class AdminUseCase {

    @Autowired
    private CreateDealership createDealership;

    @Autowired
    private CreateEmployee createEmployee;

    @Autowired
    private CreatePlate createPlateService;

    @Autowired
    private DeletePlate deletePlateService;

    @Autowired
    private DeleteEmployee deleteEmployee;

    @Autowired
    private DeleteDealership deleteDealership;

    @Autowired
    private ManageService manageService;

    @Autowired
    private EmployeePort employeePort;

    public void createMessenger(Employee employee) throws Exception {
        employee.setRole(Role.MESSENGER);
        createEmployee.create(employee);
    }

    public void createDealership(Dealership dealership) throws Exception {
        createDealership.create(dealership);
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

    public void deleteDealership(Long idDealership) throws Exception {
        deleteDealership.deleteById(idDealership);
    }

    @Transactional
    public void createPlate(Plate plate, String username, Long idDealership) throws Exception {
        createPlateService.create(plate);
        Employee query = new Employee();
        query.setUserName(username);
        Employee adminUser = employeePort.findByUserName(query);
        manageService.createServiceFromPlate(plate, adminUser, idDealership);
    }

    public void deletePlate(Long idPlate) throws Exception {
        deletePlateService.deleteById(idPlate);
    }
}