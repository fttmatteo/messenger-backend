package app.domain.services;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import app.application.exceptions.BusinessException;
import app.domain.model.Employee;
import app.domain.model.Photo;
import app.domain.model.ServiceDelivery;
import app.domain.model.Signature;
import app.domain.model.StatusHistory;
import app.domain.model.enums.Role;
import app.domain.model.enums.Status;
import app.domain.ports.EmployeePort;
import app.domain.ports.ServiceDeliveryPort;

@Service
public class UpdateServiceDelivery {

    @Autowired
    private ServiceDeliveryPort serviceDeliveryPort;
    @Autowired
    private EmployeePort employeePort;

    public void updateStatus(Long serviceId, Status newStatus, String observation,
            Signature signature, List<Photo> photos, Long userDocument) throws Exception {

        ServiceDelivery service = serviceDeliveryPort.findById(serviceId);
        if (service == null) {
            throw new BusinessException("El servicio con ID " + serviceId + " no existe.");
        }

        Employee user = employeePort.findByDocument(userDocument);
        if (user == null) {
            throw new BusinessException("El usuario con documento " + userDocument + " no existe.");
        }

        Status previousStatus = service.getCurrentStatus();

        validateStateTransition(previousStatus, newStatus, user.getRole());

        if (newStatus == Status.CANCELED || newStatus == Status.OBSERVED) {
            if (!Role.ADMIN.equals(user.getRole())) {
                throw new BusinessException("Solo el administrador puede cambiar el estado a " + newStatus);
            }
        }

        validateEvidence(newStatus, signature, photos, observation);
        service.setCurrentStatus(newStatus);

        if (observation != null && !observation.isEmpty()) {
            service.setObservation(observation);
        }

        if (signature != null) {
            signature.setUploadDate(LocalDateTime.now());
            service.setSignature(signature);
        }

        if (photos != null && !photos.isEmpty()) {
            for (Photo p : photos) {
                p.setUploadDate(LocalDateTime.now());
                service.addPhoto(p);
            }
        }

        StatusHistory history = new StatusHistory();
        history.setPreviousStatus(previousStatus);
        history.setNewStatus(newStatus);
        history.setChangeDate(LocalDateTime.now());
        history.setChangedBy(user);
        service.addHistory(history);

        serviceDeliveryPort.save(service);
    }

    private void validateEvidence(Status status, Signature signature, List<Photo> photos, String observation)
            throws BusinessException {
        if (status == Status.DELIVERED) {
            if (signature == null) {
                throw new BusinessException("Para marcar como ENTREGADO, la firma de recibido es obligatoria.");
            }
        }

        else if (status == Status.PENDING || status == Status.RETURNED || status == Status.FAILED) {
            if (signature == null) {
                throw new BusinessException("Para el estado " + status + " la firma es obligatoria.");
            }
            if (photos == null || photos.isEmpty()) {
                throw new BusinessException("Para el estado " + status + " al menos una foto es obligatoria.");
            }
            if (observation == null || observation.trim().isEmpty()) {
                throw new BusinessException("Para el estado " + status + " la observación es obligatoria.");
            }
        }
    }

    private void validateStateTransition(Status currentStatus, Status newStatus, Role userRole)
            throws BusinessException {

        if (currentStatus == newStatus) {
            throw new BusinessException("El servicio ya se encuentra en estado " + currentStatus);
        }
    }
}