package app.adapter.out.persistence.repository;

import app.adapter.out.persistence.entities.DeletedTrackingHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para historial de tracking archivado.
 */
@Repository
public interface DeletedTrackingHistoryRepository extends JpaRepository<DeletedTrackingHistoryEntity, Long> {
    List<DeletedTrackingHistoryEntity> findByServiceDeliveryIdOrderByRecordedAtAsc(Long serviceDeliveryId);
}
