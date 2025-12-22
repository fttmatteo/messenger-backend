package app.infrastructure.persistence.repository;

import app.domain.model.enums.Status;
import app.infrastructure.persistence.entities.ServiceDeliveryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data JPA para servicios de entrega.
 */
@Repository
public interface ServiceDeliveryRepository extends JpaRepository<ServiceDeliveryEntity, Long> {

        List<ServiceDeliveryEntity> findByCurrentStatus(Status currentStatus);

        List<ServiceDeliveryEntity> findByMessenger_Document(Long messengerDocument);

        List<ServiceDeliveryEntity> findByPlate_PlateNumber(String plateNumber);

        List<ServiceDeliveryEntity> findByDealership_IdDealership(Long dealershipId);

        List<ServiceDeliveryEntity> findByDeletedFalse();

        Optional<ServiceDeliveryEntity> findByIdServiceDeliveryAndDeletedFalse(Long id);

        List<ServiceDeliveryEntity> findByDeletedTrue();

        List<ServiceDeliveryEntity> findByDeletedTrueAndDeletedAtBefore(LocalDateTime date);

        List<ServiceDeliveryEntity> findByPlate_PlateNumberAndDeletedFalse(String plateNumber);

        @Query(value = """
                        SELECT DATE(created_at) as date,
                               SUM(CASE WHEN current_status = 'ASSIGNED' THEN 1 ELSE 0 END) as assigned,
                               SUM(CASE WHEN current_status = 'DELIVERED' THEN 1 ELSE 0 END) as delivered,
                               SUM(CASE WHEN current_status = 'RETURNED' THEN 1 ELSE 0 END) as returned,
                               SUM(CASE WHEN current_status = 'CANCELED' THEN 1 ELSE 0 END) as canceled,
                               COUNT(*) as total
                        FROM service_deliveries
                        WHERE messenger_id = :messengerId
                          AND created_at >= :fromDate
                          AND created_at < :toDate
                          AND deleted = false
                        GROUP BY DATE(created_at)
                        ORDER BY date DESC
                        """, nativeQuery = true)
        List<Object[]> findDailyStatsByMessenger(
                        @Param("messengerId") Long messengerId,
                        @Param("fromDate") LocalDateTime fromDate,
                        @Param("toDate") LocalDateTime toDate);
}