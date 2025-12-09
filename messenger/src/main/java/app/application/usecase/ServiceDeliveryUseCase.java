package app.application.usecase;

import java.io.File;
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
        String savedPath = storagePort.save(imageFile, "detections");
        // Usamos el archivo guardado para el OCR para asegurar consistencia
        File savedFile = storagePort.get(savedPath);

        String extractedText = ocrPort.extractText(savedFile);
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

        Signature signature = null;
        if (signatureFile != null) {
            String path = storagePort.save(signatureFile, "signatures");
            signature = new Signature();
            signature.setSignaturePath(path);
        }

        List<Photo> photos = new java.util.ArrayList<>();
        if (photoFiles != null && !photoFiles.isEmpty()) {
            for (File f : photoFiles) {
                String path = storagePort.save(f, "evidence");
                Photo p = new Photo();
                p.setPhotoPath(path);
                p.setPhotoType(app.domain.model.enums.PhotoType.EVIDENCE);
                photos.add(p);
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