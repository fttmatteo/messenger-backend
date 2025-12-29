package app.infrastructure.service;

import app.domain.model.ServiceDelivery;
import app.domain.ports.ArchivePort;
import app.infrastructure.persistence.entities.*;
import app.infrastructure.persistence.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio de infraestructura para archivar servicios eliminados.
 * Mueve servicios de la papelera al archivo permanente preservando todos los
 * datos.
 */
@Service
public class ArchiveServiceService implements ArchivePort {

    @Autowired
    private DeletedServiceRepository deletedServiceRepository;

    @Autowired
    private DeletedStatusHistoryRepository deletedStatusHistoryRepository;

    @Autowired
    private DeletedPhotoRepository deletedPhotoRepository;

    @Autowired
    private DeletedTrackingHistoryRepository deletedTrackingHistoryRepository;

    @Autowired
    private DeletedSignatureRepository deletedSignatureRepository;

    @Autowired
    private ServiceDeliveryRepository serviceDeliveryRepository;

    @Autowired
    private TrackingHistoryRepository trackingHistoryRepository;

    /**
     * Archiva un servicio permanentemente.
     * Copia todos los datos relacionados al archivo y luego borra físicamente el
     * servicio original.
     *
     * @param service             Servicio a archivar(debe estar en papelera:
     *                            deleted=true)
     * @param deletedByEmployeeId ID del empleado que realizó el archivado (puede
     *                            ser null para auto-archive)
     * @param deletionReason      Razón del archivado (ej: "Manual trash empty",
     *                            "Auto-archive after 60 days")
     */
    @Transactional
    public void archiveService(ServiceDelivery service, Long deletedByEmployeeId, String deletionReason) {
        Long serviceId = service.getIdServiceDelivery();

        // 1. Crear registro principal en deleted_services con datos desnormalizados
        DeletedServiceEntity archivedService = new DeletedServiceEntity();
        archivedService.setIdServiceDelivery(serviceId);
        archivedService.setCurrentStatus(service.getCurrentStatus());
        archivedService.setObservation(service.getObservation());
        archivedService.setCreatedAt(service.getCreatedAt());
        archivedService.setDeletedAt(service.getDeletedAt());
        archivedService.setLockedAt(service.getLockedAt());

        // Foreign key IDs (para referencia) - with null checks
        if (service.getPlate() != null) {
            archivedService.setPlateId(service.getPlate().getIdPlate());
            archivedService.setPlateNumber(service.getPlate().getPlateNumber());
            archivedService.setPlateType(service.getPlate().getPlateType() != null
                    ? service.getPlate().getPlateType().name()
                    : "UNKNOWN");
        }

        if (service.getDealership() != null) {
            archivedService.setDealershipId(service.getDealership().getIdDealership());
            archivedService.setDealershipName(service.getDealership().getName());
            archivedService.setDealershipAddress(service.getDealership().getAddress());
            archivedService.setDealershipZone(service.getDealership().getZone());
        }

        if (service.getMessenger() != null) {
            archivedService.setMessengerId(service.getMessenger().getIdEmployee());
            archivedService.setMessengerName(service.getMessenger().getFullName());
            archivedService.setMessengerDocument(service.getMessenger().getDocument() != null
                    ? service.getMessenger().getDocument().toString()
                    : null);
            archivedService.setMessengerPhone(service.getMessenger().getPhone());
        }

        if (service.getSignature() != null) {
            archivedService.setSignatureId(service.getSignature().getIdSignature());
        }

        // Metadata de archivado
        archivedService.setPermanentlyDeletedAt(LocalDateTime.now());
        archivedService.setPermanentlyDeletedBy(deletedByEmployeeId);
        archivedService.setDeletionReason(deletionReason);

        deletedServiceRepository.save(archivedService);

        // 2. Archivar historial de estados
        if (service.getHistory() != null && !service.getHistory().isEmpty()) {
            for (var history : service.getHistory()) {
                DeletedStatusHistoryEntity archivedHistory = new DeletedStatusHistoryEntity();
                archivedHistory.setIdStatusHistory(history.getIdStatusHistory());
                archivedHistory.setServiceDeliveryId(serviceId);
                archivedHistory.setPreviousStatus(history.getPreviousStatus());
                archivedHistory.setNewStatus(history.getNewStatus());
                archivedHistory.setChangeDate(history.getChangeDate());
                archivedHistory.setObservation(null); // StatusHistory domain model doesn't have observation

                // Denormalizar datos del empleado que hizo el cambio
                if (history.getChangedBy() != null) {
                    archivedHistory.setChangedByEmployeeId(history.getChangedBy().getIdEmployee());
                    archivedHistory.setChangedByName(history.getChangedBy().getFullName());
                    archivedHistory.setChangedByDocument(history.getChangedBy().getDocument().toString());
                }

                deletedStatusHistoryRepository.save(archivedHistory);
            }
        }

        // 3. Archivar fotos
        if (service.getPhotos() != null && !service.getPhotos().isEmpty()) {
            for (var photo : service.getPhotos()) {
                DeletedPhotoEntity archivedPhoto = new DeletedPhotoEntity();
                archivedPhoto.setIdPhoto(photo.getIdPhoto());
                archivedPhoto.setServiceDeliveryId(serviceId);
                archivedPhoto.setStatusHistoryId(null); // Domain model doesn't track this
                archivedPhoto.setPhotoPath(photo.getPhotoPath());
                archivedPhoto.setPhotoType(photo.getPhotoType());
                archivedPhoto.setUploadDate(photo.getUploadDate());

                deletedPhotoRepository.save(archivedPhoto);
            }
        }

        // 4. Archivar tracking history (si existe)
        List<TrackingHistoryEntity> trackingHistory = trackingHistoryRepository.findByServiceDeliveryId(serviceId);
        if (trackingHistory != null && !trackingHistory.isEmpty()) {
            for (var tracking : trackingHistory) {
                DeletedTrackingHistoryEntity archivedTracking = new DeletedTrackingHistoryEntity();
                archivedTracking.setHistoryId(tracking.getHistoryId());
                archivedTracking.setServiceDeliveryId(serviceId);
                archivedTracking.setMessengerId(tracking.getMessengerId());

                // Convertir Double a BigDecimal (TrackingHistoryEntity usa Double,
                // DeletedTracking usa BigDecimal)
                archivedTracking.setLatitude(toBigDecimal(tracking.getLatitude()));
                archivedTracking.setLongitude(toBigDecimal(tracking.getLongitude()));
                archivedTracking.setSpeed(toBigDecimal(tracking.getSpeed()));

                archivedTracking.setSource(tracking.getSource());
                archivedTracking.setRecordedAt(tracking.getRecordedAt());

                deletedTrackingHistoryRepository.save(archivedTracking);
            }
        }

        // 5. Archivar firma (si existe)
        if (service.getSignature() != null) {
            DeletedSignatureEntity archivedSignature = new DeletedSignatureEntity();
            archivedSignature.setIdSignature(service.getSignature().getIdSignature());
            archivedSignature.setServiceDeliveryId(serviceId);
            archivedSignature.setSignaturePath(service.getSignature().getSignaturePath());
            archivedSignature.setCreatedAt(LocalDateTime.now());

            deletedSignatureRepository.save(archivedSignature);
        }

        // 6. Borrar físicamente de las tablas activas
        // Obtener la entidad desde el repositorio y borrarla
        ServiceDeliveryEntity entity = serviceDeliveryRepository.findById(service.getIdServiceDelivery())
                .orElseThrow(() -> new RuntimeException("Service not found: " + service.getIdServiceDelivery()));
        serviceDeliveryRepository.delete(entity);
    }

    /**
     * Archiva múltiples servicios en batch.
     */
    @Transactional
    public void archiveServices(List<ServiceDelivery> services, Long deletedByEmployeeId, String deletionReason) {
        for (ServiceDelivery service : services) {
            archiveService(service, deletedByEmployeeId, deletionReason);
        }
    }

    /**
     * Convierte Double a BigDecimal de forma segura.
     */
    private BigDecimal toBigDecimal(Double value) {
        return value != null ? BigDecimal.valueOf(value) : null;
    }
}
