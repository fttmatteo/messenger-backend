package app.adapter.in.rest.mapper;

import app.adapter.in.rest.response.*;
import app.adapter.out.storage.GoogleCloudStorageAdapter;
import app.domain.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Mapper de ServiceDelivery a ServiceDeliveryResponse para API REST.
 */
@Component
public class ServiceDeliveryResponseMapper {

    private static final long EDIT_WINDOW_HOURS = 72;

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
                return null;
            }
        }
        return path;
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

        // Campos de bloqueo (ventana de 72 horas)
        response.setLockedAt(service.getLockedAt());
        if (service.getLockedAt() != null) {
            LocalDateTime editDeadline = service.getLockedAt().plusHours(EDIT_WINDOW_HOURS);
            response.setEditDeadline(editDeadline);
            response.setLocked(LocalDateTime.now().isAfter(editDeadline));
        } else {
            response.setLocked(false);
        }

        // Campos de papelera (soft delete)
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
            response.setSignature(new SignatureResponse(
                    sig.getIdSignature(),
                    signedUrl,
                    sig.getUploadDate()));
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
                                employeeMapper.toResponse(h.getChangedBy()));

                        if (h.getPhotos() != null) {
                            historyResponse.setPhotos(h.getPhotos().stream()
                                    .map(p -> new PhotoResponse(
                                            p.getIdPhoto(),
                                            getFileUrl(p.getPhotoPath()),
                                            p.getUploadDate(),
                                            p.getPhotoType()))
                                    .collect(Collectors.toList()));
                        }
                        return historyResponse;
                    })
                    .collect(Collectors.toList()));
        }

        return response;
    }
}
