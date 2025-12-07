package app.domain.ports;

import app.domain.model.ServiceDelivery;
import app.domain.model.enums.Status;
import java.util.List;

public interface ServiceDeliveryPort {
    ServiceDelivery save(ServiceDelivery serviceDelivery);

    ServiceDelivery update(ServiceDelivery serviceDelivery);

    void deleteById(Long idServiceDelivery);

    ServiceDelivery findById(Long idServiceDelivery);

    List<ServiceDelivery> findAll();

    // Consultas operativas útiles
    List<ServiceDelivery> findByStatus(Status status);

    List<ServiceDelivery> findByMessengerDocument(Long messengerDocument);

    List<ServiceDelivery> findByPlateNumber(String plateNumber);
}