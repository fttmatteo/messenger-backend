package app.application.usecase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import app.domain.model.Employee;
import app.domain.model.Plate;
import app.domain.services.CreatePlate;
import app.domain.services.ManageService;
import app.domain.ports.EmployeePort;

@Service
public class MessengerUseCase {

    @Autowired
    private CreatePlate createPlateService;

    @Autowired
    private ManageService manageService;

    @Autowired
    private EmployeePort employeePort;

    @Transactional
    public void createPlateAndService(Plate plate, String username) throws Exception {
        createPlateService.create(plate);
        Employee query = new Employee();
        query.setUserName(username);
        Employee messenger = employeePort.findByUserName(query);
        manageService.createServiceFromPlate(plate, messenger);
    }
}