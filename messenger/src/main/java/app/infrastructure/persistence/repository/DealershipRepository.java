package app.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import app.infrastructure.persistence.entities.DealershipEntity;

@Repository
public interface DealershipRepository extends JpaRepository<DealershipEntity, Long> {
}