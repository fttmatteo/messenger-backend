package app.domain.services;

import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import app.application.exceptions.BusinessException;
import app.domain.model.Employee;
import app.domain.model.Photo;
import app.domain.model.ServiceDelivery;
import app.domain.model.Signature;
import app.domain.model.StatusHistory;
import app.domain.model.enums.PhotoType;
import app.domain.model.enums.Role;
import app.domain.model.enums.Status;
import app.domain.ports.EmployeePort;
import app.domain.ports.PhotoPort;
import app.domain.ports.ServiceDeliveryPort;
import app.domain.ports.SignaturePort;
import app.domain.ports.StatusHistoryPort;

@Service
public class UpdateServiceDelivery {

    @Autowired
    private ServiceDeliveryPort serviceDeliveryPort;
    @Autowired
    private EmployeePort employeePort;
    @Autowired
    private SignaturePort signaturePort;
    @Autowired
    private PhotoPort photoPort;
    @Autowired
    private StatusHistoryPort historyPort;

    public ServiceDelivery updateStatus(Long serviceId, Status newStatus, Long userId, String observation,
            String signaturePath, String photoPath) throws Exception {

        ServiceDelivery service = serviceDeliveryPort.findById(serviceId);
        if (service == null)
            throw new BusinessException("Servicio no encontrado");

        Employee user = employeePort.findById(userId);
        if (user == null)
            throw new BusinessException("Usuario no encontrado");

        Status oldStatus = service.getCurrentStatus();

        validatePermissions(user, newStatus);
        validateRequirements(newStatus, signaturePath, photoPath, observation);
        service.setCurrentStatus(newStatus);
        service.setObservation(observation);
        ServiceDelivery updatedService = serviceDeliveryPort.update(service);

        if (signaturePath != null && !signaturePath.isEmpty()) {
            saveSignature(updatedService, signaturePath);
        }
        if (photoPath != null && !photoPath.isEmpty()) {
            saveEvidencePhoto(updatedService, photoPath);
        }

        saveHistory(updatedService, oldStatus, newStatus, user);
        return updatedService;
    }

    private void validatePermissions(Employee user, Status status) throws BusinessException {
        if ((status == Status.CANCELED || status == Status.OBSERVED) && user.getRole() != Role.ADMIN) {
            throw new BusinessException("Solo el administrador puede cancelar u observar servicios");
        }
    }

    private void validateRequirements(Status status, String signature, String photo, String observation)
            throws BusinessException {
        // Regla: Para estado ENTREGADO firma obligatoria, resto opcional
        if (status == Status.DELIVERED) {
            if (signature == null || signature.isEmpty()) {
                throw new BusinessException("La firma es obligatoria para entregar");
            }
        }
        // Regla: Para estados PENDIENTE, FALLIDO, DEVUELTO todo es obligatorio
        boolean isExceptionState = (status == Status.FAILED || status == Status.RETURNED || status == Status.PENDING);
        if (isExceptionState) {
            if (signature == null || signature.isEmpty())
                throw new BusinessException("La firma es obligatoria");
            if (photo == null || photo.isEmpty())
                throw new BusinessException("La foto es obligatoria");
            if (observation == null || observation.isEmpty())
                throw new BusinessException("La observación es obligatoria");
        }
    }

    private void saveSignature(ServiceDelivery service, String path) {
        Signature sig = new Signature();
        sig.setSignaturePath(path);
        sig.setUploadDate(LocalDateTime.now());
        sig.setServiceDelivery(service);
        signaturePort.save(sig);
    }

    private void saveEvidencePhoto(ServiceDelivery service, String path) {
        Photo p = new Photo();
        p.setPhotoPath(path);
        p.setPhotoType(PhotoType.EVIDENCE);
        p.setUploadDate(LocalDateTime.now());
        p.setServiceDelivery(service);
        photoPort.save(p);
    }

    private void saveHistory(ServiceDelivery service, Status oldStatus, Status newStatus, Employee user) {
        StatusHistory history = new StatusHistory();
        history.setServiceDelivery(service);
        history.setPreviousStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setChangeDate(LocalDateTime.now());
        history.setChangedBy(user);
        historyPort.save(history);
    }
}