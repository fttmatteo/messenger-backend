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
    private OcrPort ocrPort;

    public void createServiceFromImage(File imageFile, Long dealershipId, Long messengerDocument) throws Exception {
        String extractedText = ocrPort.extractText(imageFile);
        createService.create(extractedText, dealershipId, messengerDocument);
    }

    public void updateStatus(Long serviceId, Status newStatus, String observation,
            Signature signature, List<Photo> photos, Long userDocument) throws Exception {
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