package app.domain.ports;

import app.domain.model.ServiceDelivery;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Puerto de salida para persistencia de servicios de entrega.
 */
public interface ServiceDeliveryPort {

    ServiceDelivery save(ServiceDelivery serviceDelivery);

    void deleteById(Long idServiceDelivery);

    ServiceDelivery findById(Long idServiceDelivery);

    List<ServiceDelivery> findAll();

    List<ServiceDelivery> findByPlateNumber(String plateNumber);

    List<ServiceDelivery> findByMessengerId(Long messengerId);

    // Métodos para soft delete (papelera)
    List<ServiceDelivery> findAllActive();

    ServiceDelivery findByIdActive(Long idServiceDelivery);

    List<ServiceDelivery> findDeleted();

    List<ServiceDelivery> findDeletedExpiredBefore(LocalDateTime date);

    void hardDeleteById(Long idServiceDelivery);

    int hardDeleteAllDeleted();

    // Estadísticas diarias por mensajero
    List<app.domain.model.DailyStatistics> findDailyStatsByMessenger(
            Long messengerId,
            java.time.LocalDate from,
            java.time.LocalDate to);
}