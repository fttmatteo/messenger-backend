package app.domain.services;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import app.adapter.in.validators.ServiceDeliveryValidator;
import app.application.exceptions.BusinessException;
import app.domain.model.Dealership;
import app.domain.model.Employee;
import app.domain.model.Photo;
import app.domain.model.Plate;
import app.domain.model.ServiceDelivery;
import app.domain.model.Signature;
import app.domain.model.StatusHistory;
import app.domain.model.enums.PlateType;
import app.domain.model.enums.Role;
import app.domain.model.enums.Status;
import app.domain.ports.DealershipPort;
import app.domain.ports.EmployeePort;
import app.domain.ports.ServiceDeliveryPort;

@Service
public class ManageServiceDelivery {

    @Autowired
    private ServiceDeliveryPort serviceDeliveryPort;
    @Autowired
    private DealershipPort dealershipPort;
    @Autowired
    private EmployeePort employeePort;
    // Inyectamos el nuevo validador
    @Autowired
    private ServiceDeliveryValidator validator;

    // --- CREATE (Guardar) ---
    public void create(String plateNumber, Long dealershipId, Long messengerDocument) throws Exception {
        // 1. Validar existencia de actores
        Employee messenger = employeePort.findByDocument(messengerDocument);
        if (messenger == null) throw new BusinessException("El mensajero no existe");

        Dealership dealership = dealershipPort.findById(dealershipId);
        if (dealership == null) throw new BusinessException("El concesionario no existe");

        // 2. Validar y crear Placa (Usando el Validator)
        PlateType type = validator.identifyPlateType(plateNumber);
        
        Plate plate = new Plate();
        plate.setPlateNumber(plateNumber.toUpperCase());
        plate.setUploadDate(LocalDateTime.now());
        plate.setPlateType(type);

        // 3. Construir el Servicio
        ServiceDelivery service = new ServiceDelivery();
        service.setPlate(plate);
        service.setDealership(dealership);
        service.setMessenger(messenger);
        service.setCurrentStatus(Status.ASSIGNED);

        // 4. Historial Inicial
        StatusHistory history = new StatusHistory();
        history.setNewStatus(Status.ASSIGNED);
        history.setChangeDate(LocalDateTime.now());
        history.setChangedBy(messenger);
        
        service.addHistory(history);
        
        // 5. Guardar
        serviceDeliveryPort.save(service);
    }

    // --- UPDATE STATUS (Actualizar Estado) ---
    public void updateStatus(Long serviceId, Status newStatus, String username, String observation, Signature signature, Photo photo) throws Exception {
        ServiceDelivery service = serviceDeliveryPort.findById(serviceId);
        if (service == null) throw new BusinessException("Servicio no encontrado");

        Employee actor = employeePort.findByUserName(username);
        if (actor == null) throw new BusinessException("Usuario no válido");

        // Validaciones de Rol para estados administrativos
        if (newStatus == Status.CANCELED || newStatus == Status.OBSERVED) {
            if (!actor.getRole().equals(Role.ADMIN)) {
                throw new BusinessException("Solo el administrador puede realizar esta acción");
            }
        }

        // Validar requisitos del estado (Usando el Validator)
        validator.validateStatusRequirements(newStatus, signature, photo, observation);

        // Actualizar Historial
        StatusHistory history = new StatusHistory();
        history.setPreviousStatus(service.getCurrentStatus());
        history.setNewStatus(newStatus);
        history.setChangeDate(LocalDateTime.now());
        history.setChangedBy(actor);
        service.addHistory(history);

        // Actualizar Entidad
        service.setCurrentStatus(newStatus);
        
        if (observation != null) service.setObservation(observation);
        
        if (signature != null) {
            signature.setUploadDate(LocalDateTime.now());
            service.setSignature(signature);
        }
        
        if (photo != null) {
            photo.setUploadDate(LocalDateTime.now());
            service.addPhoto(photo);
        }

        serviceDeliveryPort.save(service);
    }

    // --- DELETE (Eliminar) ---
    public void delete(Long id) {
        serviceDeliveryPort.deleteById(id);
    }
}