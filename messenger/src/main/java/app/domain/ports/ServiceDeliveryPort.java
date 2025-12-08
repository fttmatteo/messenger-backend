package app.domain.ports;

import app.domain.model.ServiceDelivery;
import app.domain.model.enums.Status;
import java.util.List;

public interface ServiceDeliveryPort {
    void save(ServiceDelivery serviceDelivery);

    void deleteById(Long idServiceDelivery);

    ServiceDelivery findById(Long idServiceDelivery);

    List<ServiceDelivery> findAll();

    List<ServiceDelivery> findByStatus(Status status);

    List<ServiceDelivery> findByMessengerDocument(Long messengerDocument);

    List<ServiceDelivery> findByPlateNumber(String plateNumber);

    List<ServiceDelivery> findByDealershipId(Long dealershipId);
}