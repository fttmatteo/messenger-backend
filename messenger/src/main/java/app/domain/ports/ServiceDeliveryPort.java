// Archivo: app/domain/ports/ServiceDeliveryPort.java
package app.domain.ports;

import app.domain.model.ServiceDelivery;
import app.domain.model.enums.Status;
import java.util.List;

public interface ServiceDeliveryPort {
    // Guarda el servicio con TODAS sus dependencias (fotos, historial, firma)
    ServiceDelivery save(ServiceDelivery serviceDelivery);
    void deleteById(Long idServiceDelivery);
    // Debe retornar el objeto completo con sus listas
    ServiceDelivery findById(Long idServiceDelivery);
    List<ServiceDelivery> findAll();
    // Consultas específicas para el negocio
    List<ServiceDelivery> findByStatus(Status status);
    List<ServiceDelivery> findByMessengerDocument(Long messengerDocument);
    List<ServiceDelivery> findByPlateNumber(String plateNumber);
}