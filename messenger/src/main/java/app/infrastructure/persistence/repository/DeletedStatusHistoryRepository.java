package app.infrastructure.persistence.repository;

import app.infrastructure.persistence.entities.DeletedStatusHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para historial de estados archivados.
 */
@Repository
public interface DeletedStatusHistoryRepository extends JpaRepository<DeletedStatusHistoryEntity, Long> {

    /**
     * Encuentra el historial de estados para un servicio archivado
     */
    List<DeletedStatusHistoryEntity> findByServiceDeliveryIdOrderByChangeDateDesc(Long serviceDeliveryId);
}
