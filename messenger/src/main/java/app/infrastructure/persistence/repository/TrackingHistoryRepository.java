package app.infrastructure.persistence.repository;

import app.infrastructure.persistence.entities.TrackingHistoryEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio Spring Data para historial de tracking GPS.
 */
@Repository
public interface TrackingHistoryRepository extends CrudRepository<TrackingHistoryEntity, Long> {
        org.springframework.data.domain.Page<TrackingHistoryEntity> findByMessengerIdAndRecordedAtBetween(
                        Long messengerId,
                        LocalDateTime start,
                        LocalDateTime end,
                        org.springframework.data.domain.Pageable pageable);

        List<TrackingHistoryEntity> findByServiceDeliveryId(Long serviceDeliveryId);

        List<TrackingHistoryEntity> findTop10ByMessengerIdOrderByRecordedAtDesc(Long messengerId);

        TrackingHistoryEntity findFirstByMessengerIdOrderByRecordedAtDesc(Long messengerId);
        
        long countByMessengerIdAndRecordedAtBetween(
                        Long messengerId,
                        LocalDateTime start,
                        LocalDateTime end);
}
