package app.infrastructure.persistence.repository;

import app.infrastructure.persistence.entities.PlateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA para la entidad PlateEntity.
 * Permite la búsqueda y gestión de placas vehiculares registradas.
 */
@Repository
public interface PlateRepository extends JpaRepository<PlateEntity, Long> {
    PlateEntity findByPlateNumber(String plateNumber);
}