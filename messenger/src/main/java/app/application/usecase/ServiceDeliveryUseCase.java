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
 * - Cuando mensajero usa PENDING → bloqueado hasta que admin use
 * CANCELED/RESOLVED
 * - DELIVERED/RESOLVED → 72 horas para cambiar estado, después bloqueado
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

    @Transactional(rollbackFor = Exception.class)
    @AuditableAction(action = "CREATE_SERVICE", description = "Crear servicio desde imagen OCR")
    public ServiceDelivery createServiceFromImage(File imageFile, Long dealershipId, Long messengerId)
            throws Exception {
        logger.info("Iniciando creación de servicio desde imagen. DealershipId: {}, MessengerId: {}", dealershipId,
                messengerId);
        String extractedText = ocrPort.extractText(imageFile);
        logger.debug("Texto extraído por OCR: {}", extractedText);
        String timestamp = LocalDateTime.now().format(DATE_FORMAT);
        String fileName = extractedText + "_ASSIGNED_" + timestamp;

        String savedPath = storagePort.save(imageFile, "detections", fileName);

        try {
            ServiceDelivery service = createService.create(extractedText, savedPath, dealershipId, messengerId);
            logger.info("Servicio creado exitosamente con ID: {}", service.getIdServiceDelivery());
            return service;
        } catch (Exception e) {
            logger.error("Error creando servicio desde imagen: {}", e.getMessage());
            cleanupFiles(savedPath);
            throw e;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @AuditableAction(action = "CREATE_SERVICE_MANUAL", description = "Crear servicio con placa manual")
    public ServiceDelivery createServiceWithManualPlate(File imageFile, String manualPlateNumber, Long dealershipId,
            Long messengerId) throws Exception {
        logger.info("Iniciando creación de servicio manual. Placa: {}, DealershipId: {}, MessengerId: {}",
                manualPlateNumber, dealershipId, messengerId);
        String timestamp = LocalDateTime.now().format(DATE_FORMAT);
        String fileName = manualPlateNumber + "_ASSIGNED_" + timestamp;

        String savedPath = storagePort.save(imageFile, "detections", fileName);

        try {
            ServiceDelivery service = createService.create(manualPlateNumber, savedPath, dealershipId, messengerId);
            logger.info("Servicio manual creado exitosamente con ID: {}", service.getIdServiceDelivery());
            return service;
        } catch (Exception e) {
            logger.error("Error creando servicio manual: {}", e.getMessage());
            cleanupFiles(savedPath);
            throw e;
        }
    }

    @AuditableAction(action = "UPDATE_STATUS", description = "Actualizar estado de servicio")
    public ServiceDelivery updateStatus(Long serviceId, Status newStatus, String observation,
            Signature signature, List<Photo> photos, Long userId) throws Exception {
        logger.info("Actualizando estado de servicio ID: {} a {}", serviceId, newStatus);
        return updateService.updateStatus(serviceId, newStatus, observation, signature, photos, userId);
    }

    public ServiceDelivery updateStatusWithFiles(Long serviceId, Status newStatus, String observation,
            File signatureFile, List<File> photoFiles, Long userId) throws Exception {
        logger.info("Actualizando estado con archivos. ServiceID: {}, NuevoEstado: {}", serviceId, newStatus);

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
                    userId);
            logger.info("Estado actualizado exitosamente para servicio ID: {}", serviceId);
            return updated;
        } catch (Exception e) {
            logger.error("Error actualizando estado de servicio ID: {}: {}", serviceId, e.getMessage());
            cleanupFiles(savedPaths);
            throw e;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @AuditableAction(action = "REASSIGN_MESSENGER", description = "Reasignar servicio a otro mensajero")
    public ServiceDelivery reassignMessenger(Long serviceId, Long newMessengerId, Long adminUserId) throws Exception {
        logger.info("Reasignando servicio ID: {} a mensajero ID: {} por admin ID: {}",
                serviceId, newMessengerId, adminUserId);
        return updateService.reassignMessenger(serviceId, newMessengerId, adminUserId);
    }

    @Transactional(readOnly = true)
    public ServiceDelivery findById(Long id) throws Exception {
        return searchService.findById(id);
    }

    @Transactional(readOnly = true)
    public List<ServiceDelivery> findAll() {
        return searchService.findAll();
    }

    @Transactional(readOnly = true)
    public List<ServiceDelivery> findByPlate(String plateNumber) {
        return searchService.findByPlate(plateNumber);
    }

    public void deleteById(Long id) throws Exception {
        logger.info("Moviendo servicio ID: {} a la papelera", id);
        deleteService.deleteById(id);
    }

    @AuditableAction(action = "DELETE_SERVICE", description = "Mover servicio a papelera")
    public void deleteById(Long id, Long userId) throws Exception {
        logger.info("Moviendo servicio ID: {} a la papelera por usuario ID: {}", id, userId);
        deleteService.deleteById(id, userId);
    }

    @Transactional(readOnly = true)
    public List<ServiceDelivery> findDeleted() {
        return searchService.findDeleted();
    }

    @Transactional(rollbackFor = Exception.class)
    @AuditableAction(action = "RESTORE_SERVICE", description = "Restaurar servicio desde papelera")
    public ServiceDelivery restore(Long id, Long userId) throws Exception {
        logger.info("Restaurando servicio ID: {} de la papelera por usuario ID: {}", id, userId);
        return deleteService.restore(id, userId);
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

    @Transactional(readOnly = true)
    public List<DailyStatistics> getDailyStats(
            Long messengerId,
            java.time.LocalDate from,
            java.time.LocalDate to) {
        return searchService.findDailyStatsByMessenger(messengerId, from, to);
    }
}