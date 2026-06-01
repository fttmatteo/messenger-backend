package app.application.usecase.delivery;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import app.domain.exception.BusinessException;
import app.domain.model.Employee;
import app.domain.model.Photo;
import app.domain.model.ServiceDelivery;
import app.domain.model.Signature;
import app.domain.model.StatusHistory;
import app.domain.model.enums.Role;
import app.domain.model.enums.Status;
import app.domain.ports.EmployeePort;
import app.domain.ports.ServiceDeliveryPort;
import org.springframework.context.ApplicationEventPublisher;
import app.domain.events.PlateStatusChangedEvent;

/**
 * Servicio para actualizar estado de servicios con validación de reglas de
 * negocio.
 */
@Service
public class UpdateServiceDeliveryUseCase {

    private static final Logger logger = LoggerFactory.getLogger(UpdateServiceDeliveryUseCase.class);

    private static final Set<Status> MESSENGER_ALLOWED_STATES = Set.of(Status.PENDING, Status.DELIVERED,
            Status.RETURNED);
            
    @Autowired
    private ServiceDeliveryPort serviceDeliveryPort;
    @Autowired
    private EmployeePort employeePort;
    @Autowired
    private app.domain.ports.DealershipPort dealershipPort;
    @Autowired
    private app.domain.ports.TrackingPort trackingPort;
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    /**
     * Actualiza el estado de un servicio (Sobrecarga para compatibilidad sin
     * ubicación).
     */
    public ServiceDelivery updateStatus(Long serviceId, Status newStatus, String observation,
            Signature signature, List<Photo> photos, Long userId) throws Exception {
        return updateStatus(serviceId, newStatus, observation, signature, photos, userId, null, null);
    }

    /**
     * Actualiza el estado de un servicio, validando privilegios, transiciones y
     * evidencias.
     */
    public ServiceDelivery updateStatus(Long serviceId, Status newStatus, String observation,
            Signature signature, List<Photo> photos, Long userId, Double latitude, Double longitude) throws Exception {

        ServiceDelivery service = serviceDeliveryPort.findByIdActive(serviceId);
        if (service == null) {
            logger.warn("Fallo al actualizar servicio: no existe o está en la papelera.");
            throw new BusinessException("El servicio no existe o está en la papelera.");
        }

        Employee user = employeePort.findById(userId);
        if (user == null) {
            logger.warn("Fallo al actualizar servicio: usuario no existe.");
            throw new BusinessException("El usuario indicado no existe.");
        }

        Status previousStatus = service.getCurrentStatus();
        Role userRole = user.getRole();

        try {
            validateStateTransitionByRole(previousStatus, newStatus, userRole);
            validateEvidence(newStatus, signature, photos, observation, userRole);
        } catch (BusinessException e) {
            logger.warn("Validación de actualización fallida: de {} a {} (Rol {}). Razón: {}",
                    previousStatus, newStatus, userRole, e.getMessage());
            throw e;
        }

        service.setCurrentStatus(newStatus);

        String trimmedObservation = observation != null && !observation.trim().isEmpty() ? observation.trim() : null;
        service.setObservation(trimmedObservation);

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
        history.setDeliveryLatitude(latitude);
        history.setDeliveryLongitude(longitude);

        if (photos != null && !photos.isEmpty()) {
            history.setPhotos(new ArrayList<>(photos));
        }

        if (signature != null) {
            history.setSignature(signature);
        }

        history.setObservation(trimmedObservation);

        if (service.getOriginDealership() != null) {
            history.setSnapshotOriginDealershipId(service.getOriginDealership().getIdDealership());
            history.setSnapshotOriginDealershipName(service.getOriginDealership().getName());
        }
        if (service.getDealership() != null) {
            history.setSnapshotDestinationDealershipId(service.getDealership().getIdDealership());
            history.setSnapshotDestinationDealershipName(service.getDealership().getName());
        }

        service.addHistory(history);

        if (latitude != null && longitude != null) {
            app.domain.model.TrackingHistory tracking = new app.domain.model.TrackingHistory(
                    userId,
                    new app.domain.model.Location(latitude, longitude),
                    app.domain.model.enums.TrackingSource.MANUAL);
            tracking.setServiceDeliveryId(serviceId);
            trackingPort.saveTrackingHistory(tracking);
        }

        ServiceDelivery saved = serviceDeliveryPort.save(service);
        logger.info("Servicio actualizado exitosamente de {} a {} (Rol {}).",
                previousStatus, newStatus, user.getRole());
        eventPublisher.publishEvent(new PlateStatusChangedEvent(saved, previousStatus, newStatus));
        return saved;
    }

    /**
     * Reasigna un servicio a otro mensajero. Solo permitido para ADMIN cuando el
     * servicio está en CANCELED.
     */
    public ServiceDelivery reassignMessenger(Long serviceId, Long newMessengerId, Long adminUserId) throws Exception {
        ServiceDelivery service = serviceDeliveryPort.findByIdActive(serviceId);
        if (service == null) {
            throw new BusinessException("El servicio no existe o está en la papelera.");
        }

        Employee admin = employeePort.findById(adminUserId);
        if (admin == null || admin.getRole() != Role.ADMIN) {
            logger.warn("Intento no autorizado de reasignar servicio por usuario sin rol ADMIN.");
            throw new BusinessException("Solo los administradores pueden reasignar servicios.");
        }

        if (service.getCurrentStatus() != Status.CANCELED) {
            throw new BusinessException("Solo se pueden reasignar servicios en estado CANCELED. Estado actual: "
                    + service.getCurrentStatus());
        }

        Employee newMessenger = employeePort.findById(newMessengerId);
        if (newMessenger == null) {
            throw new BusinessException("El mensajero indicado no existe.");
        }

        if (newMessenger.getRole() != Role.MESSENGER) {
            throw new BusinessException("El empleado seleccionado no tiene rol de MENSAJERO.");
        }

        Status previousStatus = service.getCurrentStatus();
        service.setMessenger(newMessenger);
        service.setCurrentStatus(Status.ASSIGNED);

        StatusHistory history = new StatusHistory();
        history.setPreviousStatus(previousStatus);
        history.setNewStatus(Status.ASSIGNED);
        history.setChangeDate(LocalDateTime.now());
        history.setChangedBy(newMessenger);
        history.setObservation("Reasignado a " + newMessenger.getFullName() + " por administrador");

        if (service.getOriginDealership() != null) {
            history.setSnapshotOriginDealershipId(service.getOriginDealership().getIdDealership());
            history.setSnapshotOriginDealershipName(service.getOriginDealership().getName());
        }
        if (service.getDealership() != null) {
            history.setSnapshotDestinationDealershipId(service.getDealership().getIdDealership());
            history.setSnapshotDestinationDealershipName(service.getDealership().getName());
        }

        service.addHistory(history);

        ServiceDelivery saved = serviceDeliveryPort.save(service);
        logger.info("Servicio reasignado exitosamente al nuevo mensajero por administrador.");
        eventPublisher.publishEvent(new PlateStatusChangedEvent(saved, previousStatus, Status.ASSIGNED));
        return saved;
    }

    /**
     * Valida si el cambio de estado es permitido según el rol del usuario.
     */
    private void validateStateTransitionByRole(Status currentStatus, Status newStatus, Role userRole)
            throws BusinessException {

        if (currentStatus == newStatus) {
            throw new BusinessException("El servicio ya se encuentra en estado " + currentStatus);
        }

        if (userRole == Role.MESSENGER) {
            if (currentStatus == Status.SCHEDULED) {
                throw new BusinessException("No tienes permiso para modificar un servicio programado.");
            }
            if (!MESSENGER_ALLOWED_STATES.contains(newStatus)) {
                throw new BusinessException(
                        "Como mensajero solo puedes cambiar el estado a: PENDING, DELIVERED o RETURNED. " +
                                "No tienes permiso para usar el estado " + newStatus + ".");
            }
        }
        // ADMIN can change to any state.
    }

    /**
     * Activa un servicio programado cambiando su estado a ASSIGNED (ejecutado por el planificador).
     */
    public ServiceDelivery activateScheduledService(Long serviceId) throws Exception {
        ServiceDelivery service = serviceDeliveryPort.findByIdActive(serviceId);
        if (service == null) {
            throw new BusinessException("El servicio no existe o está en la papelera.");
        }
        if (service.getCurrentStatus() != Status.SCHEDULED) {
            throw new BusinessException("El servicio no está en estado programado. Estado actual: " + service.getCurrentStatus());
        }

        Status previousStatus = service.getCurrentStatus();
        service.setCurrentStatus(Status.ASSIGNED);

        StatusHistory history = new StatusHistory();
        history.setPreviousStatus(previousStatus);
        history.setNewStatus(Status.ASSIGNED);
        history.setChangeDate(LocalDateTime.now());
        history.setChangedBy(null);

        if (service.getOriginDealership() != null) {
            history.setSnapshotOriginDealershipId(service.getOriginDealership().getIdDealership());
            history.setSnapshotOriginDealershipName(service.getOriginDealership().getName());
        }
        if (service.getDealership() != null) {
            history.setSnapshotDestinationDealershipId(service.getDealership().getIdDealership());
            history.setSnapshotDestinationDealershipName(service.getDealership().getName());
        }

        service.addHistory(history);

        ServiceDelivery saved = serviceDeliveryPort.save(service);
        logger.info("Servicio programado activado exitosamente a ASSIGNED por el sistema.");
        eventPublisher.publishEvent(new PlateStatusChangedEvent(saved, previousStatus, Status.ASSIGNED));
        return saved;
    }

    public void validateEvidence(Status status, Signature signature, List<Photo> photos, String observation, Role userRole)
            throws BusinessException {

        if (userRole == Role.ADMIN) {
            return;
        }

        if (status == Status.CANCELED || status == Status.RESOLVED || status == Status.ASSIGNED) {
            return;
        }

        if (status == Status.DELIVERED && signature == null) {
            throw new BusinessException("Para marcar como ENTREGADO, la firma de recibido es obligatoria.");
        }
    }

    /**
     * Edita el origen y/o destino de un servicio. Solo ADMIN, solo en CANCELED.
     * Funciona igual que reassignMessenger: requiere que el servicio esté cancelado.
     * Registra el cambio en el historial con snapshot del estado anterior.
     */
    public ServiceDelivery editRoute(Long serviceId, Long newDealershipId, Long newOriginDealershipId,
            Long adminUserId) throws Exception {

        ServiceDelivery service = serviceDeliveryPort.findByIdActive(serviceId);
        if (service == null) {
            throw new BusinessException("El servicio no existe o está en la papelera.");
        }

        Employee admin = employeePort.findById(adminUserId);
        if (admin == null || admin.getRole() != Role.ADMIN) {
            logger.warn("Intento no autorizado de editar ruta de servicio por usuario sin rol ADMIN.");
            throw new BusinessException("Solo los administradores pueden editar la ruta del servicio.");
        }

        if (service.getCurrentStatus() != Status.CANCELED) {
            throw new BusinessException("Solo se puede editar la ruta de servicios en estado CANCELED. Estado actual: "
                    + service.getCurrentStatus());
        }

        StatusHistory history = new StatusHistory();
        history.setPreviousStatus(Status.CANCELED);
        history.setNewStatus(Status.CANCELED); 
        history.setChangeDate(LocalDateTime.now());
        history.setChangedBy(admin);
        history.setObservation("Edición de ruta por administrador");

        app.domain.model.Dealership newDealership = null;
        app.domain.model.Dealership newOriginDealership = null;

        if (newDealershipId != null) {
            newDealership = dealershipPort.findById(newDealershipId);
            if (newDealership == null) {
                throw new BusinessException("El concesionario de destino indicado no existe.");
            }
        }

        if (newOriginDealershipId != null) {
            newOriginDealership = dealershipPort.findById(newOriginDealershipId);
            if (newOriginDealership == null) {
                throw new BusinessException("El concesionario de origen indicado no existe.");
            }
        }

        Long finalDealershipId = newDealershipId != null ? newDealershipId
                : service.getDealership().getIdDealership();
        Long finalOriginId = newOriginDealershipId != null ? newOriginDealershipId
                : service.getOriginDealership().getIdDealership();
        if (finalDealershipId.equals(finalOriginId)) {
            throw new BusinessException("El concesionario de origen no puede ser el mismo que el destino.");
        }

        if (newDealership != null) {
            service.setDealership(newDealership);
        }
        if (newOriginDealership != null) {
            service.setOriginDealership(newOriginDealership);
        }

        if (service.getOriginDealership() != null) {
            history.setSnapshotOriginDealershipId(service.getOriginDealership().getIdDealership());
            history.setSnapshotOriginDealershipName(service.getOriginDealership().getName());
        }
        if (service.getDealership() != null) {
            history.setSnapshotDestinationDealershipId(service.getDealership().getIdDealership());
            history.setSnapshotDestinationDealershipName(service.getDealership().getName());
        }

        service.addHistory(history);

        ServiceDelivery saved = serviceDeliveryPort.save(service);
        logger.info("Ruta del servicio editada por administrador. Origen y/o destino actualizados.");
        return saved;
    }
}