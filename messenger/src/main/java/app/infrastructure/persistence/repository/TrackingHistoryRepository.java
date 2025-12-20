package app.infrastructure.persistence.repository;

import app.infrastructure.persistence.entities.TrackingHistoryEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TrackingHistoryRepository extends CrudRepository<TrackingHistoryEntity, Long> {
        List<TrackingHistoryEntity> findByMessengerIdAndRecordedAtBetween(
                        Long messengerId,
                        LocalDateTime start,
                        LocalDateTime end);

        List<TrackingHistoryEntity> findByServiceDeliveryId(Long serviceDeliveryId);

        List<TrackingHistoryEntity> findTop10ByMessengerIdOrderByRecordedAtDesc(Long messengerId);

        long countByMessengerIdAndRecordedAtBetween(
                        Long messengerId,
                        LocalDateTime start,
                        LocalDateTime end);
}
