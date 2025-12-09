package app.application.usecase;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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

    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public void createServiceFromImage(File imageFile, Long dealershipId, Long messengerDocument) throws Exception {
        String extractedText = ocrPort.extractText(imageFile);
        String timestamp = LocalDateTime.now().format(DATE_FORMAT);
        String fileName = extractedText + "_ASSIGNED_" + timestamp;

        String savedPath = storagePort.save(imageFile, "detections", fileName);

        try {
            createService.create(extractedText, savedPath, dealershipId, messengerDocument);
        } catch (Exception e) {
            try {
                java.nio.file.Files.deleteIfExists(java.nio.file.Paths.get(savedPath));
            } catch (Exception deleteError) {
                System.err.println("No se pudo eliminar la imagen: " + deleteError.getMessage());
            }
            throw e;
        }
    }

    public void updateStatus(Long serviceId, Status newStatus, String observation,
            Signature signature, List<Photo> photos, Long userDocument) throws Exception {
        updateService.updateStatus(serviceId, newStatus, observation, signature, photos, userDocument);
    }

    public void updateStatusWithFiles(Long serviceId, Status newStatus, String observation,
            File signatureFile, List<File> photoFiles, Long userDocument) throws Exception {

        ServiceDelivery service = searchService.findById(serviceId);
        if (service == null) {
            throw new Exception("Servicio no encontrado con ID: " + serviceId);
        }
        String plateNumber = service.getPlate().getPlateNumber();

        String timestamp = LocalDateTime.now().format(DATE_FORMAT);

        List<String> savedPaths = new java.util.ArrayList<>();

        Signature signature = null;
        if (signatureFile != null) {
            String signatureFileName = "signature_" + plateNumber + "_" + newStatus.name() + "_" + timestamp;
            String path = storagePort.save(signatureFile, "signatures", signatureFileName);
            savedPaths.add(path);
            signature = new Signature();
            signature.setSignaturePath(path);
        }

        List<Photo> photos = new java.util.ArrayList<>();
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
            updateService.updateStatus(serviceId, newStatus, observation, signature, photos, userDocument);
        } catch (Exception e) {
            for (String path : savedPaths) {
                try {
                    java.nio.file.Files.deleteIfExists(java.nio.file.Paths.get(path));
                } catch (Exception deleteError) {
                    System.err.println("No se pudo eliminar archivo: " + deleteError.getMessage());
                }
            }
            throw e;
        }
    }

    public ServiceDelivery findById(Long id) throws Exception {
        return searchService.findById(id);
    }

    public List<ServiceDelivery> findAll() {
        return searchService.findAll();
    }

    public List<ServiceDelivery> findByMessenger(Long messengerId) {
        return searchService.findByMessenger(messengerId);
    }

    public List<ServiceDelivery> findByPlate(String plateNumber) {
        return searchService.findByPlate(plateNumber);
    }

    public List<ServiceDelivery> findByDealership(Long dealershipId) {
        return searchService.findByDealership(dealershipId);
    }

    public List<ServiceDelivery> findByStatus(Status status) {
        return searchService.findByStatus(status);
    }

    public void deleteById(Long id) throws Exception {
        deleteService.deleteById(id);
    }
}