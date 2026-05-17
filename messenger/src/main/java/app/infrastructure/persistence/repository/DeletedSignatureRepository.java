package app.infrastructure.persistence.repository;

import app.infrastructure.persistence.entities.DeletedSignatureEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para firmas archivadas.
 */
@Repository
public interface DeletedSignatureRepository extends JpaRepository<DeletedSignatureEntity, Long> {
    Optional<DeletedSignatureEntity> findByServiceDeliveryId(Long serviceDeliveryId);
}
