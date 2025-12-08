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
        
        // 1. Recuperar Servicio y Usuario
        ServiceDelivery service = serviceDeliveryPort.findById(serviceId);
        if (service == null) {
            throw new BusinessException("El servicio de entrega no existe.");
        }

        Employee user = employeePort.findByDocument(userDocument);
        if (user == null) {
            throw new BusinessException("Usuario no válido para realizar la operación.");
        }

        Status previousStatus = service.getCurrentStatus();

        // 2. Validar Reglas de Roles para Estados Especiales
        if (newStatus == Status.CANCELED || newStatus == Status.OBSERVED) {
            if (!Role.ADMIN.equals(user.getRole())) {
                throw new BusinessException("Solo el administrador puede cambiar el estado a " + newStatus);
            }
        }

        // 3. Validar Reglas de Evidencia según el Nuevo Estado
        validateEvidence(newStatus, signature, photos, observation);

        // 4. Actualizar Datos del Servicio
        service.setCurrentStatus(newStatus);
        
        // Si hay observación nueva, la actualizamos (o concatenamos según lógica de negocio)
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

        // 5. Registrar Historial de Auditoría
        StatusHistory history = new StatusHistory();
        history.setPreviousStatus(previousStatus);
        history.setNewStatus(newStatus);
        history.setChangeDate(LocalDateTime.now());
        history.setChangedBy(user); // Quién hizo el cambio
        
        service.addHistory(history);

        // 6. Guardar Cambios
        serviceDeliveryPort.save(service);
    }

    private void validateEvidence(Status status, Signature signature, List<Photo> photos, String observation) throws BusinessException {
        // Regla: Para ENTREGADO, firma obligatoria. Foto y obs opcionales.
        if (status == Status.DELIVERED) {
            if (signature == null) {
                throw new BusinessException("Para marcar como ENTREGADO, la firma de recibido es obligatoria.");
            }
            // Foto y Observación son opcionales aquí, no lanzamos error si faltan.
        } 
        // Regla: Para PENDIENTE, DEVUELTO, FALLIDO (y otros flujos operativos no admin), todo obligatorio.
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
        // Para ASSIGNED, CANCELED, OBSERVED, RESOLVED no se especificaron reglas estrictas de evidencia en el prompt,
        // pero se asume que Admin gestiona CANCELED/OBSERVED sin necesidad de firmar él mismo en campo.
    }
}