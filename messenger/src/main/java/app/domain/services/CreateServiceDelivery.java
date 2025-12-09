package app.domain.services;

import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import app.application.exceptions.BusinessException;
import app.domain.model.Dealership;
import app.domain.model.Employee;
import app.domain.model.Photo;
import app.domain.model.Plate;
import app.domain.model.ServiceDelivery;
import app.domain.model.StatusHistory;
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
    private PlateRecognition plateRecognition;

    public void create(String plateNumber, String photoPath, Long dealershipId, Long messengerDocument)
            throws Exception {
        Employee messenger = employeePort.findByDocument(messengerDocument);
        if (messenger == null) {
            throw new BusinessException("El mensajero no existe.");
        }

        Dealership dealership = dealershipPort.findById(dealershipId);
        if (dealership == null) {
            throw new BusinessException("El concesionario indicado no existe.");
        }

        String normalizedPlate = plateNumber.trim().toUpperCase();

        Plate plate = platePort.findByPlateNumber(normalizedPlate);
        if (plate == null) {
            plate = new Plate();
            plate.setPlateNumber(normalizedPlate);
            plate.setPlateType(plateRecognition.determinePlateType(normalizedPlate));
            plate.setUploadDate(LocalDateTime.now());
            // Nota: La entidad Plate representa el vehículo en general.
            // La foto específica de ESTA detección se guarda en el servicio.
        }

        ServiceDelivery service = new ServiceDelivery();
        service.setPlate(plate);
        service.setDealership(dealership);
        service.setMessenger(messenger);
        service.setCurrentStatus(Status.ASSIGNED);
        service.setObservation(null);

        // Guardar la foto de detección
        if (photoPath != null) {
            Photo detectionPhoto = new Photo();
            detectionPhoto.setPhotoPath(photoPath);
            detectionPhoto.setPhotoType(app.domain.model.enums.PhotoType.PLATE_DETECTION);
            detectionPhoto.setUploadDate(LocalDateTime.now());
            service.addPhoto(detectionPhoto);
        }

        StatusHistory history = new StatusHistory();
        history.setPreviousStatus(null);
        history.setNewStatus(Status.ASSIGNED);
        history.setChangeDate(LocalDateTime.now());
        history.setChangedBy(messenger);

        service.addHistory(history);

        serviceDeliveryPort.save(service);
    }
}