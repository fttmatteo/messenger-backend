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
    public ServiceDelivery createServiceFromImage(File imageFile, Long dealershipId, Long messengerId)
            throws Exception {
        String extractedText = ocrPort.extractText(imageFile);
        String timestamp = LocalDateTime.now().format(DATE_FORMAT);
        String fileName = extractedText + "_ASSIGNED_" + timestamp;

        String savedPath = storagePort.save(imageFile, "detections", fileName);

        try {
            return createService.create(extractedText, savedPath, dealershipId, messengerId);
        } catch (Exception e) {
            cleanupFiles(savedPath);
            throw e;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public ServiceDelivery createServiceWithManualPlate(File imageFile, String manualPlateNumber, Long dealershipId,
            Long messengerId) throws Exception {
        String timestamp = LocalDateTime.now().format(DATE_FORMAT);
        String fileName = manualPlateNumber + "_ASSIGNED_" + timestamp;

        String savedPath = storagePort.save(imageFile, "detections", fileName);

        try {
            return createService.create(manualPlateNumber, savedPath, dealershipId, messengerId);
        } catch (Exception e) {
            cleanupFiles(savedPath);
            throw e;
        }
    }

    public ServiceDelivery updateStatus(Long serviceId, Status newStatus, String observation,
            Signature signature, List<Photo> photos, Long userId) throws Exception {
        return updateService.updateStatus(serviceId, newStatus, observation, signature, photos, userId);
    }

    public ServiceDelivery updateStatusWithFiles(Long serviceId, Status newStatus, String observation,
            File signatureFile, List<File> photoFiles, Long userId) throws Exception {

        ServiceDelivery service = searchService.findById(serviceId);
        if (service == null) {
            throw new Exception("Servicio no encontrado con ID: " + serviceId);
        }
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
            return updateService.updateStatus(serviceId, newStatus, observation, signature, photos, userId);
        } catch (Exception e) {
            cleanupFiles(savedPaths);
            throw e;
        }
    }

    public ServiceDelivery findById(Long id) throws Exception {
        return searchService.findById(id);
    }

    public List<ServiceDelivery> findAll() {
        return searchService.findAll();
    }

    public List<ServiceDelivery> findByPlate(String plateNumber) {
        return searchService.findByPlate(plateNumber);
    }

    public void deleteById(Long id) throws Exception {
        deleteService.deleteById(id);
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
}