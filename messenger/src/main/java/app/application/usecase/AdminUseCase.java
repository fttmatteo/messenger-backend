package app.application.usecase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import app.domain.model.Dealership;
import app.domain.model.Employee;
import app.domain.model.Plate;
import app.domain.model.enums.Role;
import app.domain.ports.EmployeePort;
import app.domain.services.*; // Importamos todos los servicios del dominio

@Service
public class AdminUseCase {

    // --- Servicios de Creación ---
    @Autowired
    private CreateDealership createDealershipService;
    @Autowired
    private CreateEmployee createEmployeeService;
    @Autowired
    private CreatePlate createPlateService;
    @Autowired
    private CreateService createServiceDomain; // RENOMBRADO: Antes era 'manageService'

    // --- Servicios de Eliminación ---
    @Autowired
    private DeletePlate deletePlateService;
    @Autowired
    private DeleteEmployee deleteEmployeeService;
    @Autowired
    private DeleteDealership deleteDealershipService;

    // --- Puertos ---
    @Autowired
    private EmployeePort employeePort;

    public void createMessenger(Employee employee) throws Exception {
        employee.setRole(Role.MESSENGER);
        // Las validaciones de unicidad (cédula/username) ocurren ADENTRO de este
        // servicio.
        // No es necesario exponerlas en el UseCase.
        createEmployeeService.create(employee);
    }

    public void createDealership(Dealership dealership) throws Exception {
        createDealershipService.create(dealership);
    }

    public void deleteEmployee(Long document) throws Exception {
        deleteEmployeeService.deleteByDocument(document);
    }

    public void deleteDealership(Long idDealership) throws Exception {
        deleteDealershipService.deleteById(idDealership);
    }

    public void deletePlate(Long idPlate) throws Exception {
        deletePlateService.deleteById(idPlate);
    }

    @Transactional
    public void createPlate(Plate plate, String username, Long idDealership) throws Exception {
        // 1. Crear la placa
        createPlateService.create(plate);

        // 2. Buscar al administrador que está ejecutando la acción
        Employee query = new Employee();
        query.setUserName(username);
        Employee adminUser = employeePort.findByUserName(query);

        // 3. Crear el servicio asociado a la placa
        createServiceDomain.createServiceFromPlate(plate, adminUser, idDealership);
    }
}