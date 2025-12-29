package app.domain.services;

import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import app.domain.exception.BusinessException;
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

/**
 * Servicio para crear nuevos servicios de entrega con reconocimiento de placa.
 */
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
    @Autowired
    private app.domain.ports.TrackingPort trackingPort;

    /**
     * Crea un nuevo servicio de entrega, asocia la placa (creándola si no existe)
     * y asigna el servicio al mensajero y concesionario indicados.
     */
    public ServiceDelivery create(String plateNumber, String photoPath, Long dealershipId, Long messengerId,
            Double latitude, Double longitude)
            throws Exception {

        Employee messenger = employeePort.findById(messengerId);
        if (messenger == null) {
            throw new BusinessException("El mensajero no existe.");
        }

        Dealership dealership = dealershipPort.findById(dealershipId);
        if (dealership == null) {
            throw new BusinessException("El concesionario indicado no existe.");
        }

        String normalizedPlate = plateNumber.trim().toUpperCase();

        if (!serviceDeliveryPort.findByPlateNumber(normalizedPlate).isEmpty()) {
            throw new BusinessException(
                    "La placa " + normalizedPlate + " ya tiene un servicio registrado en el sistema.");
        }

        Plate plate = platePort.findByPlateNumber(normalizedPlate);
        if (plate == null) {
            plate = new Plate();
            plate.setPlateNumber(normalizedPlate);
            plate.setPlateType(plateRecognition.determinePlateType(normalizedPlate));
            plate.setUploadDate(LocalDateTime.now());
            platePort.save(plate);
        }

        ServiceDelivery service = new ServiceDelivery();
        service.setPlate(plate);
        service.setDealership(dealership);
        service.setMessenger(messenger);
        service.setCurrentStatus(Status.ASSIGNED);
        service.setObservation(null);

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
        history.setDeliveryLatitude(latitude);
        history.setDeliveryLongitude(longitude);

        service.addHistory(history);

        ServiceDelivery saved = serviceDeliveryPort.save(service);

        // Save initial tracking location if provided
        if (latitude != null && longitude != null) {
            app.domain.model.TrackingHistory tracking = new app.domain.model.TrackingHistory();
            tracking.setMessengerId(messenger.getIdEmployee());
            tracking.setServiceDeliveryId(saved.getIdServiceDelivery());

            app.domain.model.Location location = new app.domain.model.Location(
                    latitude,
                    longitude,
                    LocalDateTime.now(),
                    0.0 // accuracy default
            );
            tracking.setLocation(location);

            tracking.setRecordedAt(LocalDateTime.now());
            tracking.setSource(app.domain.model.enums.TrackingSource.MANUAL);
            trackingPort.saveTrackingHistory(tracking);
        }

        return saved;
    }
}