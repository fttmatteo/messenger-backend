package app.domain.services;

import app.application.exceptions.BusinessException;
import app.domain.model.Photo;
import app.domain.model.Service;
import app.domain.model.Signature;
import app.domain.model.StatusHistory;
import app.domain.model.Employee;
import app.domain.model.enums.Status;
import app.domain.ports.EmployeePort;
import app.domain.ports.ServicePort;
import org.springframework.beans.factory.annotation.Autowired;
import java.time.LocalDateTime;

@org.springframework.stereotype.Service
public class UpdateService {

    @Autowired
    private ServicePort servicePort;
    @Autowired
    private EmployeePort employeePort;

    // AHORA recibe los objetos de dominio Signature y Photo, no Strings.
    public void updateStatus(Long idService, Status newStatus, String observation,
            Signature signature, Photo photo, String username, String userRole) throws Exception {

        Service service = servicePort.findById(idService);
        if (service == null)
            throw new BusinessException("Servicio no encontrado");

        // 1. Validaciones de transición de estado
        validateTransition(service.getStatus(), newStatus, userRole, signature, photo, observation);

        // 2. Asociar Foto (Si viene una)
        if (photo != null) {
            photo.setService(service);
            service.setVisitPhoto(photo);
        }

        // 3. Asociar Firma (Si viene una)
        if (signature != null) {
            // Buscamos al empleado que firma (Lógica de Negocio: vincular al usuario
            // actual)
            Employee signerProbe = new Employee();
            signerProbe.setUserName(username);
            Employee fullSigner = employeePort.findByUserName(signerProbe);

            signature.setEmployee(fullSigner); // Asociamos empleado
            signature.setService(service); // Asociamos servicio
            service.setSignature(signature);
        }

        // 4. Actualizar Estado
        Status oldStatus = service.getStatus();
        service.setStatus(newStatus);
        service.setObservation(observation);
        if (newStatus == Status.DELIVERED) {
            service.setDeliveredDate(LocalDateTime.now());
        }

        // 5. Agregar Historial
        addToHistory(service, oldStatus, newStatus, username, observation);

        servicePort.save(service);
    }

    private void validateTransition(Status current, Status next, String role, Signature sig, Photo photo, String obs)
            throws BusinessException {
        if (next == Status.CANCELED || next == Status.OBSERVED) {
            if (!"ADMIN".equalsIgnoreCase(role)) {
                throw new BusinessException("Solo el administrador puede cambiar el estado a " + next);
            }
        }
        if (next == Status.DELIVERED && sig == null) {
            throw new BusinessException("Para marcar como ENTREGADO, la firma es obligatoria.");
        } else if ((next == Status.PENDING || next == Status.RETURNED || next == Status.FAILED)) {
            if (sig == null)
                throw new BusinessException("Firma obligatoria");
            if (photo == null)
                throw new BusinessException("Foto obligatoria");
            if (obs == null || obs.trim().isEmpty())
                throw new BusinessException("Observación obligatoria");
        }
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