package app.application.usecase;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import app.domain.model.DailyStatistics;
import app.domain.model.Photo;
import app.domain.model.ServiceDelivery;
import app.domain.model.Signature;
import app.domain.model.enums.Status;
import app.domain.ports.OcrPort;
import app.domain.ports.StoragePort;
import app.domain.services.CreateServiceDelivery;
import app.domain.services.DeleteServiceDelivery;
import app.domain.services.SearchServiceDelivery;
import app.domain.services.UpdateServiceDelivery;
import app.infrastructure.audit.AuditableAction;

/**
 * Caso de uso principal para gestión de servicios de entrega.
 * 
 * Reglas de negocio implementadas:
 * - Creación automática con estado ASSIGNED al mensajero autenticado
 * - Mensajero: solo puede usar PENDING, DELIVERED, RETURNED
 * - Admin: solo puede usar CANCELED, RESOLVED y reasignar mensajero
 * - Eliminación → Papelera (soft delete) con borrado definitivo a los 60 días
 */
@Service
public class ServiceDeliveryUseCase {

    private static final Logger logger = LoggerFactory.getLogger(ServiceDeliveryUseCase.class);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    @Autowired
    private CreateServiceDelivery createService;
    @Autowired
    private UpdateServiceDelivery updateService;
    @Autowired
    private SearchServiceDelivery searchService;
    @Autowired
    private DeleteServiceDelivery deleteService;
    @Autowired
    private StoragePort storagePort;
    @Autowired
    private OcrPort ocrPort;

    /**
     * Crea un servicio a partir de una imagen de placa procesada por OCR.
     */
    @Transactional(rollbackFor = Exception.class)
    @AuditableAction(action = "CREATE_SERVICE", description = "Crear servicio desde imagen OCR")
    public ServiceDelivery createServiceFromImage(File imageFile, Long dealershipId, Long messengerId, Double latitude,
            Double longitude)
            throws Exception {
        // ...existing code...
        String extractedText = ocrPort.extractText(imageFile);
        // ...existing code...
        String timestamp = LocalDateTime.now().format(DATE_FORMAT);
        String fileName = extractedText + "_ASSIGNED_" + timestamp;

        String savedPath = storagePort.save(imageFile, "detections", fileName);

        try {
            ServiceDelivery service = createService.create(extractedText, savedPath, dealershipId, messengerId,
                    latitude, longitude);
            logger.info("Servicio creado exitosamente vía OCR - ID: {} | Placa: {} | Mensajero: {}",
                    service.getIdServiceDelivery(), extractedText, messengerId);
            return service;
        } catch (Exception e) {
            logger.error("Error creando servicio desde imagen: {}", e.getMessage());
            cleanupFiles(savedPath);
            throw e;
        }
    }

    /**
     * Crea un servicio utilizando un número de placa ingresado manualmente.
     */
    @Transactional(rollbackFor = Exception.class)
    @AuditableAction(action = "CREATE_SERVICE_MANUAL", description = "Crear servicio con placa manual")
    public ServiceDelivery createServiceWithManualPlate(File imageFile, String manualPlateNumber, Long dealershipId,
            Long messengerId, Double latitude, Double longitude) throws Exception {
        // ...existing code...
        String timestamp = LocalDateTime.now().format(DATE_FORMAT);
        String fileName = manualPlateNumber + "_ASSIGNED_" + timestamp;

        String savedPath = storagePort.save(imageFile, "detections", fileName);

        try {
            ServiceDelivery service = createService.create(manualPlateNumber, savedPath, dealershipId, messengerId,
                    latitude, longitude);
            logger.info("Servicio creado exitosamente vía manual - ID: {} | Placa: {} | Mensajero: {}",
                    service.getIdServiceDelivery(), manualPlateNumber, messengerId);
            return service;
        } catch (Exception e) {
            logger.error("Error creando servicio manual: {}", e.getMessage());
            cleanupFiles(savedPath);
            throw e;
        }
    }

    /**
     * Actualiza el estado de un servicio existente (Sobrecarga compatible).
     */
    public ServiceDelivery updateStatus(Long serviceId, Status newStatus, String observation,
            Signature signature, List<Photo> photos, Long userId) throws Exception {
        return updateStatus(serviceId, newStatus, observation, signature, photos, userId, null, null);
    }

    /**
     * Actualiza el estado de un servicio existente.
     */
    @AuditableAction(action = "UPDATE_STATUS", description = "Actualizar estado de servicio")
    public ServiceDelivery updateStatus(Long serviceId, Status newStatus, String observation,
            Signature signature, List<Photo> photos, Long userId, Double latitude, Double longitude) throws Exception {
        // ...existing code...
        return updateService.updateStatus(serviceId, newStatus, observation, signature, photos, userId, latitude,
                longitude);
    }

    /**
     * Actualiza el estado de un servicio incluyendo la carga de archivos (fotos,
     * firmas).
     */
    public ServiceDelivery updateStatusWithFiles(Long serviceId, Status newStatus, String observation,
            File signatureFile, List<File> photoFiles, Long userId, Double latitude, Double longitude)
            throws Exception {
        // ...existing code...

        ServiceDelivery service = searchService.findById(serviceId);
        String plateNumber = service.getPlate().getPlateNumber();
        String timestamp = LocalDateTime.now().format(DATE_FORMAT);

        List<String> savedPaths = new ArrayList<>();

        Signature signature = null;
        if (signatureFile != null) {
            String signatureFileName = "signature_" + plateNumber + "_" + newStatus.name() + "_" + timestamp;
            String path = storagePort.save(signatureFile, "signatures", signatureFileName);
            savedPaths.add(path);
            signature = new Signature();
            signature.setSignaturePath(path);
        }

        List<Photo> photos = new ArrayList<>();
        if (photoFiles != null && !photoFiles.isEmpty()) {
            int count = 1;
            for (File f : photoFiles) {
                String evidenceFileName = "evidence_" + plateNumber + "_" + newStatus.name() + "_" + timestamp + "_"
                        + count;
                String path = storagePort.save(f, "evidence", evidenceFileName);
                savedPaths.add(path);
                Photo p = new Photo();
                p.setPhotoPath(path);
                p.setPhotoType(app.domain.model.enums.PhotoType.EVIDENCE);
                photos.add(p);
                count++;
            }
        }

        try {
            ServiceDelivery updated = updateService.updateStatus(serviceId, newStatus, observation, signature, photos,
                    userId, latitude, longitude);
            logger.info("Estado de servicio actualizado - ID: {} | Nuevo Estado: {} | Usuario: {}",
                    serviceId, newStatus, userId);
            return updated;
        } catch (Exception e) {
            logger.error("Error actualizando estado de servicio ID: {}: {}", serviceId, e.getMessage());
            cleanupFiles(savedPaths);
            throw e;
        }
    }

    /**
     * Reasigna un servicio cancelado a un nuevo mensajero (solo admin).
     */
    @Transactional(rollbackFor = Exception.class)
    @AuditableAction(action = "REASSIGN_MESSENGER", description = "Reasignar servicio a otro mensajero")
    public ServiceDelivery reassignMessenger(Long serviceId, Long newMessengerId, Long adminUserId) throws Exception {
        // ...existing code...
        return updateService.reassignMessenger(serviceId, newMessengerId, adminUserId);
    }

    /**
     * Busca un servicio por su ID (versión de solo lectura).
     */
    @Transactional(readOnly = true)
    public ServiceDelivery findById(Long id) throws Exception {
        return searchService.findById(id);
    }

    /**
     * Recupera todos los servicios registrados.
     */
    @Transactional(readOnly = true)
    public List<ServiceDelivery> findAll() {
        return searchService.findAll();
    }

    /**
     * Recupera todos los servicios con paginación y ordenamiento.
     */
    @Transactional(readOnly = true)
    public Page<ServiceDelivery> findAllPaginated(int page, int size, String sortBy, String sortDirection,
            String search) {
        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return searchService.findAllPaginated(search, false, pageable);
    }

    /**
     * Recupera servicios de un mensajero específico con paginación.
     */
    @Transactional(readOnly = true)
    public Page<ServiceDelivery> findByMessengerPaginated(Long messengerId, int page, int size, String sortBy,
            String sortDirection, String search) {
        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return searchService.findByMessengerPaginated(messengerId, search, false, pageable);
    }

    /**
     * Busca servicios asociados a un número de placa específico.
     */
    @Transactional(readOnly = true)
    public List<ServiceDelivery> findByPlate(String plateNumber) {
        return searchService.findByPlate(plateNumber);
    }

    /**
     * Mueve un servicio a la papelera (método simple sin auditoría de usuario
     * explícito).
     */
    public void deleteById(Long id) throws Exception {
        // ...existing code...
        deleteService.deleteById(id);
    }

    /**
     * Mueve un servicio a la papelera (Soft Delete).
     */
    @AuditableAction(action = "DELETE_SERVICE", description = "Mover servicio a papelera")
    public void deleteById(Long id, Long userId) throws Exception {
        // ...existing code...
        deleteService.deleteById(id, userId);
    }

    /**
     * Recupera todos los servicios que han sido movidos a la papelera.
     */
    @Transactional(readOnly = true)
    public List<ServiceDelivery> findDeleted() {
        return searchService.findDeleted();
    }

    /**
     * Restaura un servicio previamente eliminado de la papelera.
     */
    @Transactional(rollbackFor = Exception.class)
    @AuditableAction(action = "RESTORE_SERVICE", description = "Restaurar servicio desde papelera")
    public ServiceDelivery restore(Long id, Long userId) throws Exception {
        // ...existing code...
        return deleteService.restore(id, userId);
    }

    /**
     * Vacía la papelera eliminando permanentemente todos los servicios marcados
     * como eliminados.
     */
    @Transactional(rollbackFor = Exception.class)
    @AuditableAction(action = "EMPTY_TRASH", description = "Vaciar papelera completamente")
    public int emptyTrash(Long userId) {
        // ...existing code...
        return deleteService.emptyTrash();
    }

    /**
     * Elimina permanentemente un servicio específico de la papelera.
     */
    @Transactional(rollbackFor = Exception.class)
    @AuditableAction(action = "ARCHIVE_SERVICE", description = "Archivar permanentemente un servicio de la papelera")
    public void permanentDeleteById(Long id, Long userId) throws Exception {
        // ...existing code...
        deleteService.archiveService(id);
    }

    private void cleanupFiles(String... paths) {
        for (String path : paths) {
            try {
                Files.deleteIfExists(Paths.get(path));
            } catch (Exception e) {
                logger.warn("No se pudo eliminar archivo: {}", e.getMessage());
            }
        }
    }

    private void cleanupFiles(List<String> paths) {
        cleanupFiles(paths.toArray(new String[0]));
    }

    /**
     * Genera estadísticas diarias de entregas para un mensajero en un rango de
     * fechas.
     */
    @Transactional(readOnly = true)
    public List<DailyStatistics> getDailyStats(
            Long messengerId,
            java.time.LocalDate from,
            java.time.LocalDate to) {
        return searchService.findDailyStatsByMessenger(messengerId, from, to);
    }
}