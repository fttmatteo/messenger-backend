package app.domain.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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

/**
 * Servicio para actualizar estado de servicios con validación de reglas de
 * negocio.
 * 
 * Reglas implementadas:
 * - Mensajero solo puede usar: PENDING, DELIVERED, RETURNED
 * - Admin solo puede usar: CANCELED, RESOLVED y reasignar mensajero (solo si
 * está en CANCELED)
 * - Cuando mensajero usa PENDING → bloqueado hasta que admin use
 * CANCELED/RESOLVED
 * - DELIVERED/RESOLVED → 72 horas para cambiar estado/info, después bloqueado
 */
@Service
public class UpdateServiceDelivery {

    private static final long EDIT_WINDOW_HOURS = 72;
    private static final Set<Status> MESSENGER_ALLOWED_STATES = Set.of(Status.PENDING, Status.DELIVERED,
            Status.RETURNED);
    private static final Set<Status> ADMIN_ALLOWED_STATES = Set.of(Status.CANCELED, Status.RESOLVED);
    private static final Set<Status> LOCKED_STATES = Set.of(Status.DELIVERED, Status.RESOLVED);

    @Autowired
    private ServiceDeliveryPort serviceDeliveryPort;
    @Autowired
    private EmployeePort employeePort;

    public ServiceDelivery updateStatus(Long serviceId, Status newStatus, String observation,
            Signature signature, List<Photo> photos, Long userId) throws Exception {

        ServiceDelivery service = serviceDeliveryPort.findByIdActive(serviceId);
        if (service == null) {
            throw new BusinessException("El servicio con ID " + serviceId + " no existe o está en la papelera.");
        }

        Employee user = employeePort.findById(userId);
        if (user == null) {
            throw new BusinessException("El usuario con ID " + userId + " no existe.");
        }

        Status previousStatus = service.getCurrentStatus();
        Role userRole = user.getRole();

        // Validar ventana de 72 horas para estados bloqueados
        validateEditWindow(service);

        // Validar transición de estados según rol
        validateStateTransitionByRole(previousStatus, newStatus, userRole);

        // Validar evidencias requeridas
        validateEvidence(newStatus, signature, photos, observation);

        // Actualizar estado
        service.setCurrentStatus(newStatus);

        // Establecer bloqueo si el nuevo estado es DELIVERED o RESOLVED
        if (LOCKED_STATES.contains(newStatus) && service.getLockedAt() == null) {
            service.setLockedAt(LocalDateTime.now());
        }

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

        if (photos != null && !photos.isEmpty()) {
            history.setPhotos(new ArrayList<>(photos));
        }

        service.addHistory(history);

        return serviceDeliveryPort.save(service);
    }

    /**
     * Reasigna un servicio a otro mensajero. Solo permitido para ADMIN cuando el
     * servicio está en CANCELED.
     */
    public ServiceDelivery reassignMessenger(Long serviceId, Long newMessengerId, Long adminUserId) throws Exception {
        ServiceDelivery service = serviceDeliveryPort.findByIdActive(serviceId);
        if (service == null) {
            throw new BusinessException("El servicio con ID " + serviceId + " no existe o está en la papelera.");
        }

        Employee admin = employeePort.findById(adminUserId);
        if (admin == null || admin.getRole() != Role.ADMIN) {
            throw new BusinessException("Solo los administradores pueden reasignar servicios.");
        }

        if (service.getCurrentStatus() != Status.CANCELED) {
            throw new BusinessException("Solo se pueden reasignar servicios en estado CANCELED. Estado actual: "
                    + service.getCurrentStatus());
        }

        Employee newMessenger = employeePort.findById(newMessengerId);
        if (newMessenger == null) {
            throw new BusinessException("El mensajero con ID " + newMessengerId + " no existe.");
        }

        if (newMessenger.getRole() != Role.MESSENGER) {
            throw new BusinessException("El empleado seleccionado no tiene rol de MENSAJERO.");
        }

        Status previousStatus = service.getCurrentStatus();
        service.setMessenger(newMessenger);
        service.setCurrentStatus(Status.ASSIGNED);
        service.setLockedAt(null); // Limpiar bloqueo al reasignar

        StatusHistory history = new StatusHistory();
        history.setPreviousStatus(previousStatus);
        history.setNewStatus(Status.ASSIGNED);
        history.setChangeDate(LocalDateTime.now());
        history.setChangedBy(admin);
        service.addHistory(history);

        return serviceDeliveryPort.save(service);
    }

    private void validateEditWindow(ServiceDelivery service) throws BusinessException {
        if (service.getLockedAt() != null) {
            LocalDateTime editDeadline = service.getLockedAt().plusHours(EDIT_WINDOW_HOURS);
            if (LocalDateTime.now().isAfter(editDeadline)) {
                throw new BusinessException(
                        "El período de edición de 72 horas ha expirado. " +
                                "El servicio fue bloqueado el " + service.getLockedAt() +
                                " y la fecha límite fue " + editDeadline + ".");
            }
        }
    }

    private void validateStateTransitionByRole(Status currentStatus, Status newStatus, Role userRole)
            throws BusinessException {

        if (currentStatus == newStatus) {
            throw new BusinessException("El servicio ya se encuentra en estado " + currentStatus);
        }

        // Estados finales: CANCELED y RESOLVED - nadie puede cambiarlos
        if (currentStatus == Status.CANCELED || currentStatus == Status.RESOLVED) {
            throw new BusinessException(
                    "El servicio está en estado final " + currentStatus + " y no se puede modificar.");
        }

        if (userRole == Role.MESSENGER) {
            // Mensajero solo puede usar PENDING, DELIVERED, RETURNED
            if (!MESSENGER_ALLOWED_STATES.contains(newStatus)) {
                throw new BusinessException(
                        "Como mensajero solo puedes cambiar el estado a: PENDING, DELIVERED o RETURNED. " +
                                "No tienes permiso para usar el estado " + newStatus + ".");
            }

            // Si está en PENDING, el mensajero no puede hacer más cambios
            if (currentStatus == Status.PENDING) {
                throw new BusinessException(
                        "El servicio está en estado PENDING. Solo un administrador puede cambiarlo a CANCELED o RESOLVED.");
            }

            // Si está en DELIVERED, el mensajero no puede hacer más cambios
            if (currentStatus == Status.DELIVERED) {
                throw new BusinessException(
                        "El servicio ya fue marcado como ENTREGADO. Solo un administrador puede modificar su estado.");
            }
        }

        if (userRole == Role.ADMIN) {
            // Admin solo puede usar CANCELED y RESOLVED
            if (!ADMIN_ALLOWED_STATES.contains(newStatus)) {
                throw new BusinessException(
                        "Como administrador solo puedes cambiar el estado a: CANCELED o RESOLVED. " +
                                "Para otros estados, el mensajero asignado debe realizar el cambio.");
            }
        }
    }

    private void validateEvidence(Status status, Signature signature, List<Photo> photos, String observation)
            throws BusinessException {

        // Estados que no requieren evidencia
        if (status == Status.CANCELED || status == Status.RESOLVED || status == Status.ASSIGNED) {
            return;
        }

        if (status == Status.DELIVERED) {
            if (signature == null) {
                throw new BusinessException("Para marcar como ENTREGADO, la firma de recibido es obligatoria.");
            }
            return;
        }

        // PENDING y RETURNED requieren firma, foto y observación
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