package app.domain.ports;

import app.domain.model.ServiceDelivery;
import java.util.List;

public interface ServiceDeliveryPort {

    ServiceDelivery save(ServiceDelivery serviceDelivery);

    void deleteById(Long idServiceDelivery);

    ServiceDelivery findById(Long idServiceDelivery);

    List<ServiceDelivery> findAll();

    List<ServiceDelivery> findByPlateNumber(String plateNumber);
}