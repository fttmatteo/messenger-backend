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

    public void createServiceFromImage(File imageFile, Long dealershipId, Long messengerDocument) throws Exception {
        // Primero extraemos el texto para obtener el número de placa
        String extractedText = ocrPort.extractText(imageFile);

        // Generamos nombre: PLACA_FECHA.ext (ej: COZ92E_20231209_143052.jpeg)
        String timestamp = LocalDateTime.now().format(DATE_FORMAT);
        String fileName = extractedText + "_" + timestamp;

        // Guardamos con nombre personalizado
        String savedPath = storagePort.save(imageFile, "detections", fileName);

        createService.create(extractedText, savedPath, dealershipId, messengerDocument);
    }

    // Mantenemos este método para compatibilidad interna o tests, pero agregamos el
    // que maneja Archivos
    public void updateStatus(Long serviceId, Status newStatus, String observation,
            Signature signature, List<Photo> photos, Long userDocument) throws Exception {
        updateService.updateStatus(serviceId, newStatus, observation, signature, photos, userDocument);
    }

    public void updateStatusWithFiles(Long serviceId, Status newStatus, String observation,
            File signatureFile, List<File> photoFiles, Long userDocument) throws Exception {

        String timestamp = LocalDateTime.now().format(DATE_FORMAT);

        Signature signature = null;
        if (signatureFile != null) {
            String signatureFileName = "signature_" + timestamp;
            String path = storagePort.save(signatureFile, "signatures", signatureFileName);
            signature = new Signature();
            signature.setSignaturePath(path);
        }

        List<Photo> photos = new java.util.ArrayList<>();
        if (photoFiles != null && !photoFiles.isEmpty()) {
            int count = 1;
            for (File f : photoFiles) {
                // Nombre: evidence_FECHA_N.ext (ej: evidence_20231209_143052_1.jpeg)
                String evidenceFileName = "evidence_" + timestamp + "_" + count;
                String path = storagePort.save(f, "evidence", evidenceFileName);
                Photo p = new Photo();
                p.setPhotoPath(path);
                p.setPhotoType(app.domain.model.enums.PhotoType.EVIDENCE);
                photos.add(p);
                count++;
            }
        }

        updateService.updateStatus(serviceId, newStatus, observation, signature, photos, userDocument);
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