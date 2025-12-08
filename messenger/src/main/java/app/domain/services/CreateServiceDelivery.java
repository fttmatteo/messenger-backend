package app.domain.services;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import app.application.exceptions.BusinessException;
import app.domain.model.Dealership;
import app.domain.model.Employee;
import app.domain.model.Plate;
import app.domain.model.ServiceDelivery;
import app.domain.model.StatusHistory;
import app.domain.model.enums.Role;
import app.domain.model.enums.Status;
import app.domain.ports.DealershipPort;
import app.domain.ports.EmployeePort;
import app.domain.ports.PlatePort;
import app.domain.ports.ServiceDeliveryPort;

@Service
public class CreateServiceDelivery {

    @Autowired
    private ServiceDeliveryPort serviceDeliveryPort;
    @Autowired
    private PlatePort platePort;
    @Autowired
    private DealershipPort dealershipPort;
    @Autowired
    private EmployeePort employeePort;
    @Autowired
    private PlateRecognitionService plateRecognitionService;

    public void create(String plateNumber, Long dealershipId, Long messengerDocument) throws Exception {
        // 1. Validar Mensajero
        Employee messenger = employeePort.findByDocument(messengerDocument);
        if (messenger == null) {
            throw new BusinessException("El mensajero no existe.");
        }
        // Opcional: Validar que sea rol MESSENGER si es una regla estricta, 
        // aunque el admin también podría crear servicios.

        // 2. Validar Concesionario
        Dealership dealership = dealershipPort.findById(dealershipId);
        if (dealership == null) {
            throw new BusinessException("El concesionario indicado no existe.");
        }

        // 3. Procesar Placa (OCR lógico)
        // Buscamos si la placa ya existe en BD, si no, la creamos al vuelo (según contexto de "ingreso por foto")
        // Nota: Asumimos que plateNumber llega normalizado o crudo del OCR.
        String normalizedPlate = plateNumber.trim().toUpperCase();
        
        Plate plate = platePort.findByPlateNumber(normalizedPlate);
        if (plate == null) {
            plate = new Plate();
            plate.setPlateNumber(normalizedPlate);
            plate.setPlateType(plateRecognitionService.determinePlateType(normalizedPlate));
            plate.setUploadDate(LocalDateTime.now());
            // Aquí se guardaría la placa si PlatePort tuviera save, 
            // pero como ServiceDeliveryPort guarda todo el agregado (cascade), lo asociamos al servicio.
        }

        // 4. Crear Servicio
        ServiceDelivery service = new ServiceDelivery();
        service.setPlate(plate);
        service.setDealership(dealership);
        service.setMessenger(messenger);
        service.setCurrentStatus(Status.ASSIGNED); // Regla: Estado automático al ingresar
        service.setObservation(null); // Al inicio no hay observación de entrega

        // 5. Crear Historial Inicial
        StatusHistory history = new StatusHistory();
        history.setPreviousStatus(null);
        history.setNewStatus(Status.ASSIGNED);
        history.setChangeDate(LocalDateTime.now());
        history.setChangedBy(messenger); // El usuario que escaneó
        
        service.addHistory(history);

        // 6. Persistir
        serviceDeliveryPort.save(service);
    }
}