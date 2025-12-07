package app.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import app.infrastructure.persistence.entities.PlateEntity;

@Repository
public interface PlateRepository extends JpaRepository<PlateEntity, Long> {
    PlateEntity findByPlateNumber(String plateNumber);
}