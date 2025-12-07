package app.domain.services;

import app.application.exceptions.BusinessException;
import app.domain.model.Employee;
import app.domain.model.Photo;
import app.domain.model.Signature;
import app.domain.model.enums.FileType;
import app.domain.model.enums.PhotoType;
import app.domain.model.enums.Status;
import app.domain.model.Service;
import app.domain.model.StatusHistory;
import app.domain.ports.EmployeePort;
import app.domain.ports.ServicePort;
import org.springframework.beans.factory.annotation.Autowired;
import java.time.LocalDateTime;

@org.springframework.stereotype.Service
public class ManageService {

    @Autowired
    private ServicePort servicePort;

    @Autowired
    private EmployeePort employeePort;

    public void createServiceFromPlate(app.domain.model.Plate plate, Employee messenger) throws Exception {
        Service service = new Service();
        service.setPlate(plate);
        service.setEmployee(messenger);
        service.setStatus(Status.ASSIGNED);
        service.setAssignedDate(LocalDateTime.now());
        service.setObservation("Asignación inicial automática");
        service.setPlateType(plate.getPlateType());
        addToHistory(service, null, Status.ASSIGNED, messenger.getUserName(), "Asignación inicial automática");
        servicePort.save(service);
    }

    public void updateStatus(Long idService, Status newStatus, String observation,
            String signatureUrl, String photoUrl, String username, String userRole) throws Exception {

        Service service = servicePort.findById(idService);
        if (service == null)
            throw new BusinessException("Servicio no encontrado");

        if (newStatus == Status.CANCELED || newStatus == Status.OBSERVED) {
            if (!"ADMIN".equalsIgnoreCase(userRole)) {
                throw new BusinessException("Solo el administrador puede cambiar el estado a " + newStatus);
            }
        }

        if (newStatus == Status.DELIVERED && (signatureUrl == null || signatureUrl.trim().isEmpty())) {
            throw new BusinessException("Para marcar como ENTREGADO, la firma es obligatoria.");
        } else if ((newStatus == Status.PENDING || newStatus == Status.RETURNED || newStatus == Status.FAILED)) {
            if (signatureUrl == null || signatureUrl.trim().isEmpty())
                throw new BusinessException("Firma obligatoria");
            if (photoUrl == null || photoUrl.trim().isEmpty())
                throw new BusinessException("Foto obligatoria");
            if (observation == null || observation.trim().isEmpty())
                throw new BusinessException("Observación obligatoria");
        }

        Status oldStatus = service.getStatus();

        if (signatureUrl != null && !signatureUrl.isEmpty()) {
            Signature signature = new Signature();
            signature.setSignaturePath(signatureUrl);
            signature.setUploadDate(LocalDateTime.now());
            Employee actionEmployee = new Employee();
            actionEmployee.setUserName(username);
            Employee fullEmployee = employeePort.findByUserName(actionEmployee);
            signature.setEmployee(fullEmployee);

            service.setSignature(signature);
        }

        if (photoUrl != null && !photoUrl.isEmpty()) {
            Photo photo = new Photo();
            photo.setPhotoPath(photoUrl);
            photo.setUploadDate(LocalDateTime.now());
            photo.setFileType(FileType.PHOTO);
            photo.setPhotoPurpose(PhotoType.VISIT);

            service.setVisitPhoto(photo);
        }

        service.setStatus(newStatus);
        service.setObservation(observation);

        LocalDateTime now = LocalDateTime.now();
        if (newStatus == Status.DELIVERED)
            service.setDeliveredDate(now);

        addToHistory(service, oldStatus, newStatus, username, observation);
        servicePort.save(service);
    }

    private void addToHistory(Service service, Status oldStatus, Status newStatus, String username,
            String observation) {
        StatusHistory history = new StatusHistory();
        history.setPreviousStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setChangeDate(LocalDateTime.now());
        history.setObservation(observation);
        history.setModifiedBy(username);
        history.setService(service);
        service.getStatusHistory().add(history);
    }
}