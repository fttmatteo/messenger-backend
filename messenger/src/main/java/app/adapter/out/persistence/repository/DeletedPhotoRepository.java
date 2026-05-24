package app.adapter.out.persistence.repository;

import app.adapter.out.persistence.entities.DeletedPhotoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para fotos archivadas.
 */
@Repository
public interface DeletedPhotoRepository extends JpaRepository<DeletedPhotoEntity, Long> {
    List<DeletedPhotoEntity> findByServiceDeliveryId(Long serviceDeliveryId);
}
