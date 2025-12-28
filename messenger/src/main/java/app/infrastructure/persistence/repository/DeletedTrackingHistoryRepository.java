package app.infrastructure.persistence.repository;

import app.infrastructure.persistence.entities.DeletedTrackingHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para historial de tracking archivado.
 */
@Repository
public interface DeletedTrackingHistoryRepository extends JpaRepository<DeletedTrackingHistoryEntity, Long> {

    /**
     * Encuentra el tracking history para un servicio archivado
     */
    List<DeletedTrackingHistoryEntity> findByServiceDeliveryIdOrderByRecordedAtAsc(Long serviceDeliveryId);
}
