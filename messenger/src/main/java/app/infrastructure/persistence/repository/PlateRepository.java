package app.infrastructure.persistence.repository;

import app.infrastructure.persistence.entities.PlateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio Spring Data JPA para placas vehiculares.
 */
@Repository
public interface PlateRepository extends JpaRepository<PlateEntity, Long> {
    PlateEntity findByPlateNumber(String plateNumber);
}