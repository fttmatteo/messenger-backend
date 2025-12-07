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

    // ✅ Inyecciones nuevas para garantizar el flujo de Servicio
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

    // ✅ MÉTODO ACTUALIZADO: Crea Placa + Servicio (Flujo completo)
    @Transactional
    public void createPlate(Plate plate, String username) throws Exception {
        // 1. Guardar la Placa (física)
        createPlateService.create(plate);

        // 2. Buscar al usuario Admin que está registrando (para la auditoría)
        Employee query = new Employee();
        query.setUserName(username);
        Employee adminUser = employeePort.findByUserName(query);

        // 3. Crear el Servicio asociado usando la lógica centralizada
        // Nota: El servicio quedará asignado inicialmente a este Admin.
        // Si prefieres que quede sin asignar, podrías pasar 'null' aquí
        // y ajustar ManageService para tolerar empleados nulos.
        manageService.createServiceFromPlate(plate, adminUser);
    }

    public void deletePlate(Long idPlate) throws Exception {
        deletePlateService.deleteById(idPlate);
    }
}