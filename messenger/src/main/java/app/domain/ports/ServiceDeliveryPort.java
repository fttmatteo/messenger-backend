package app.domain.ports;

import app.domain.model.ServiceDelivery;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Puerto de salida para persistencia de servicios de entrega.
 */
public interface ServiceDeliveryPort {
        ServiceDelivery save(ServiceDelivery serviceDelivery);

        void deleteById(Long idServiceDelivery);

        ServiceDelivery findById(Long idServiceDelivery);

        ServiceDelivery findByIdActive(Long idServiceDelivery);

        ServiceDelivery findByUuidActive(String uuid);

        ServiceDelivery findByUuidIncludingDeleted(String uuid);

        Page<ServiceDelivery> findDeleted(Pageable pageable);

        Page<ServiceDelivery> findAllPaginated(String keyword, Boolean deleted,
                        List<app.domain.model.enums.Status> statuses, Pageable pageable);

        Page<ServiceDelivery> findByMessengerPaginated(Long messengerId, String keyword, Boolean deleted,
                        List<app.domain.model.enums.Status> statuses, Pageable pageable);

        List<ServiceDelivery> findDeletedExpiredBefore(LocalDateTime date);

        void hardDeleteById(Long idServiceDelivery);

        int hardDeleteAllDeleted();

        List<app.domain.model.DailyStatistics> findDailyStatsByMessenger(
                        Long messengerId,
                        java.time.LocalDate from,
                        java.time.LocalDate to);

        Page<ServiceDelivery> findByMessengerAndDate(Long messengerId, java.time.LocalDate date, Pageable pageable);

        Page<ServiceDelivery> findByPlateAndDealershipPaginated(String plateNumber, Long dealershipId, Pageable pageable);

        Page<ServiceDelivery> findByDealershipIdAndStatusesPaginated(Long dealershipId,
                        List<app.domain.model.enums.Status> statuses, Pageable pageable);
}
