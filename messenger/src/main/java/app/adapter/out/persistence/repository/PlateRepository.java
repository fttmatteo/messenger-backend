package app.adapter.out.persistence.repository;

import app.adapter.out.persistence.entities.PlateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio Spring Data JPA para placas vehiculares.
 */
@Repository
public interface PlateRepository extends JpaRepository<PlateEntity, Long> {
    PlateEntity findByPlateNumber(String plateNumber);
}