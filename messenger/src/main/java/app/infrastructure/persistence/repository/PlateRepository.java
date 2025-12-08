package app.infrastructure.persistence.repository;

import app.infrastructure.persistence.entities.PlateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PlateRepository extends JpaRepository<PlateEntity, Long> {
    Optional<PlateEntity> findByPlateNumber(String plateNumber);
}