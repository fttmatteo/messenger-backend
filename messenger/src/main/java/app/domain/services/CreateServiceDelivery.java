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
import app.domain.model.enums.PhotoType;
import app.domain.model.enums.Status;
import app.domain.ports.DealershipPort;
import app.domain.ports.EmployeePort;
import app.domain.ports.PhotoPort;
import app.domain.ports.ServiceDeliveryPort;
import app.domain.ports.StatusHistoryPort;

@Service
public class CreateServiceDelivery {

    @Autowired
    private ServiceDeliveryPort serviceDeliveryPort;
    @Autowired
    private DealershipPort dealershipPort;
    @Autowired
    private EmployeePort employeePort;
    @Autowired
    private PhotoPort photoPort;
    @Autowired
    private StatusHistoryPort historyPort;
    @Autowired
    private OcrService ocrService;
    @Autowired
    private PlateService plateService;

    public ServiceDelivery createFromPhoto(String photoPath, Long messengerId, Long dealershipId) throws Exception {

        Employee messenger = employeePort.findById(messengerId);
        if (messenger == null)
            throw new BusinessException("El mensajero no existe");

        Dealership dealership = dealershipPort.findById(dealershipId);
        if (dealership == null)
            throw new BusinessException("El concesionario no existe");

        String detectedText = ocrService.extractText(photoPath);
        Plate plate = plateService.findOrRegister(detectedText);
        ServiceDelivery service = new ServiceDelivery();
        service.setPlate(plate);
        service.setDealership(dealership);
        service.setMessenger(messenger);
        service.setCurrentStatus(Status.ASSIGNED);
        ServiceDelivery savedService = serviceDeliveryPort.save(service);
        saveDetectionPhoto(savedService, photoPath);
        saveInitialHistory(savedService, messenger);
        return savedService;
    }

    private void saveDetectionPhoto(ServiceDelivery service, String path) {
        Photo photo = new Photo();
        photo.setPhotoPath(path);
        photo.setPhotoType(PhotoType.PLATE_DETECTION);
        photo.setUploadDate(LocalDateTime.now());
        photo.setServiceDelivery(service);
        photoPort.save(photo);
    }

    private void saveInitialHistory(ServiceDelivery service, Employee messenger) {
        StatusHistory history = new StatusHistory();
        history.setServiceDelivery(service);
        history.setPreviousStatus(null);
        history.setNewStatus(Status.ASSIGNED);
        history.setChangeDate(LocalDateTime.now());
        history.setChangedBy(messenger);
        historyPort.save(history);
    }
}