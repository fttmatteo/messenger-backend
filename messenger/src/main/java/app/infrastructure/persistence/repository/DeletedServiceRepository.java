package app.infrastructure.persistence.repository;

import app.infrastructure.persistence.entities.DeletedServiceEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio para acceso a servicios archivados.
 */
@Repository
public interface DeletedServiceRepository extends JpaRepository<DeletedServiceEntity, Long> {
    Page<DeletedServiceEntity> findAllByOrderByPermanentlyDeletedAtDesc(Pageable pageable);

    List<DeletedServiceEntity> findByMessengerId(Long messengerId);

    List<DeletedServiceEntity> findByDealershipId(Long dealershipId);

    List<DeletedServiceEntity> findByPermanentlyDeletedAtBetween(LocalDateTime start, LocalDateTime end);

    List<DeletedServiceEntity> findByPlateNumber(String plateNumber);
}
