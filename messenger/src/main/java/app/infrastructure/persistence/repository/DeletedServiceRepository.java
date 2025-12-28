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

    /**
     * Encuentra todos los servicios archivados ordenados por fecha de archivo (más
     * recientes primero)
     */
    Page<DeletedServiceEntity> findAllByOrderByPermanentlyDeletedAtDesc(Pageable pageable);

    /**
     * Encuentra servicios archivados por mensajero
     */
    List<DeletedServiceEntity> findByMessengerId(Long messengerId);

    /**
     * Encuentra servicios archivados por concesionario
     */
    List<DeletedServiceEntity> findByDealershipId(Long dealershipId);

    /**
     * Encuentra servicios archivados en un rango de fechas
     */
    List<DeletedServiceEntity> findByPermanentlyDeletedAtBetween(LocalDateTime start, LocalDateTime end);

    /**
     * Encuentra servicios archivados por número de placa
     */
    List<DeletedServiceEntity> findByPlateNumber(String plateNumber);
}
