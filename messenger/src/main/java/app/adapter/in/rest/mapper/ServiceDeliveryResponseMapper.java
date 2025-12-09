package app.adapter.in.rest.mapper;

import app.adapter.in.rest.response.*;
import app.domain.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class ServiceDeliveryResponseMapper {

    @Autowired
    private EmployeeResponseMapper employeeMapper;

    @Autowired
    private DealershipResponseMapper dealershipMapper;

    public ServiceDeliveryResponse toResponse(ServiceDelivery service) {
        if (service == null) {
            return null;
        }

        ServiceDeliveryResponse response = new ServiceDeliveryResponse();
        response.setIdServiceDelivery(service.getIdServiceDelivery());
        response.setCurrentStatus(service.getCurrentStatus());
        response.setObservation(service.getObservation());

        // Map Plate
        if (service.getPlate() != null) {
            Plate plate = service.getPlate();
            response.setPlate(new ServiceDeliveryResponse.PlateResponse(
                    plate.getIdPlate(),
                    plate.getPlateNumber(),
                    plate.getPlateType()));
        }

        // Map Dealership
        response.setDealership(dealershipMapper.toResponse(service.getDealership()));

        // Map Messenger
        response.setMessenger(employeeMapper.toResponse(service.getMessenger()));

        // Map Signature
        if (service.getSignature() != null) {
            Signature sig = service.getSignature();
            response.setSignature(new SignatureResponse(
                    sig.getIdSignature(),
                    sig.getSignaturePath(),
                    sig.getUploadDate()));
        }

        // Map Photos
        if (service.getPhotos() != null) {
            response.setPhotos(service.getPhotos().stream()
                    .map(p -> new PhotoResponse(
                            p.getIdPhoto(),
                            p.getPhotoPath(),
                            p.getUploadDate(),
                            p.getPhotoType()))
                    .collect(Collectors.toList()));
        }

        // Map History
        if (service.getHistory() != null) {
            response.setHistory(service.getHistory().stream()
                    .map(h -> new StatusHistoryResponse(
                            h.getIdStatusHistory(),
                            h.getPreviousStatus(),
                            h.getNewStatus(),
                            h.getChangeDate(),
                            employeeMapper.toResponse(h.getChangedBy())))
                    .collect(Collectors.toList()));
        }

        return response;
    }
}
