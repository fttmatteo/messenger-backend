package app.adapter.in.rest.mapper;

import app.adapter.in.rest.response.*;
import app.adapter.out.storage.GoogleCloudStorageAdapter;
import app.domain.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.stream.Collectors;

/**
 * Mapper de ServiceDelivery a ServiceDeliveryResponse para API REST.
 */
@Component
public class ServiceDeliveryResponseMapper {

    private static final Logger logger = LoggerFactory.getLogger(ServiceDeliveryResponseMapper.class);

    @Autowired
    private EmployeeResponseMapper employeeMapper;
    @Autowired
    private DealershipResponseMapper dealershipMapper;
    @Autowired(required = false)
    private GoogleCloudStorageAdapter storageAdapter;

    private String getFileUrl(String path) {
        if (storageAdapter != null && path != null) {
            try {
                return storageAdapter.regenerateSignedUrl(path);
            } catch (Exception e) {
                logger.warn("Error al regenerar el URL firmado para la ruta {}: {}", path, e.getMessage());
                return null;
            }
        }
        return path;
    }

    /**
     * Mapea un ServiceDelivery a una respuesta resumida (ligera).
     * Excluye relaciones pesadas como historial y fotos para evitar consultas N+1.
     */
    public ServiceDeliveryResponse toSummaryResponse(ServiceDelivery service) {
        if (service == null) {
            return null;
        }

        ServiceDeliveryResponse response = new ServiceDeliveryResponse();
        response.setIdServiceDelivery(service.getIdServiceDelivery());
        response.setCurrentStatus(service.getCurrentStatus());
        response.setObservation(service.getObservation());
        response.setCreatedAt(service.getCreatedAt());

        response.setLockedAt(service.getLockedAt());
        response.setLocked(false);

        response.setDeleted(service.isDeleted());
        response.setDeletedAt(service.getDeletedAt());

        if (service.getPlate() != null) {
            Plate plate = service.getPlate();
            response.setPlate(new ServiceDeliveryResponse.PlateResponse(
                    plate.getIdPlate(),
                    plate.getPlateNumber(),
                    plate.getPlateType()));
        }

        response.setDealership(dealershipMapper.toResponse(service.getDealership()));
        response.setMessenger(employeeMapper.toResponse(service.getMessenger()));

        return response;
    }

    public ServiceDeliveryResponse toResponse(ServiceDelivery service) {
        if (service == null) {
            return null;
        }

        ServiceDeliveryResponse response = new ServiceDeliveryResponse();
        response.setIdServiceDelivery(service.getIdServiceDelivery());
        response.setCurrentStatus(service.getCurrentStatus());
        response.setObservation(service.getObservation());
        response.setCreatedAt(service.getCreatedAt());

        response.setLockedAt(service.getLockedAt());
        response.setLocked(false);

        response.setDeleted(service.isDeleted());
        response.setDeletedAt(service.getDeletedAt());

        if (service.getPlate() != null) {
            Plate plate = service.getPlate();
            response.setPlate(new ServiceDeliveryResponse.PlateResponse(
                    plate.getIdPlate(),
                    plate.getPlateNumber(),
                    plate.getPlateType()));
        }

        response.setDealership(dealershipMapper.toResponse(service.getDealership()));
        response.setMessenger(employeeMapper.toResponse(service.getMessenger()));

        if (service.getSignature() != null) {
            Signature sig = service.getSignature();
            String signedUrl = getFileUrl(sig.getSignaturePath());
            String gifSignedUrl = getFileUrl(sig.getGifPath());
            response.setSignature(new SignatureResponse(
                    sig.getIdSignature(),
                    signedUrl,
                    sig.getUploadDate(),
                    gifSignedUrl));
        }

        if (service.getPhotos() != null) {
            response.setPhotos(service.getPhotos().stream()
                    .map(p -> new PhotoResponse(
                            p.getIdPhoto(),
                            getFileUrl(p.getPhotoPath()),
                            p.getUploadDate(),
                            p.getPhotoType()))
                    .collect(Collectors.toList()));
        }

        if (service.getHistory() != null) {
            response.setHistory(service.getHistory().stream()
                    .map(h -> {
                        StatusHistoryResponse historyResponse = new StatusHistoryResponse(
                                h.getIdStatusHistory(),
                                h.getPreviousStatus(),
                                h.getNewStatus(),
                                h.getChangeDate(),
                                employeeMapper.toResponse(h.getChangedBy()),
                                h.getDeliveryLatitude(),
                                h.getDeliveryLongitude(),
                                h.getObservation());

                        if (h.getPhotos() != null) {
                            historyResponse.setPhotos(h.getPhotos().stream()
                                    .map(p -> new PhotoResponse(
                                            p.getIdPhoto(),
                                            getFileUrl(p.getPhotoPath()),
                                            p.getUploadDate(),
                                            p.getPhotoType()))
                                    .collect(Collectors.toList()));
                        }
                        if (h.getSignature() != null) {
                            Signature sig = h.getSignature();
                            historyResponse.setSignature(new SignatureResponse(
                                    sig.getIdSignature(),
                                    getFileUrl(sig.getSignaturePath()),
                                    sig.getUploadDate(),
                                    getFileUrl(sig.getGifPath())));
                        }
                        return historyResponse;
                    })
                    .collect(Collectors.toList()));
        }

        return response;
    }
}
