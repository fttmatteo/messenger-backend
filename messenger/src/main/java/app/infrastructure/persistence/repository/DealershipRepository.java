package app.infrastructure.persistence.repository;

import app.infrastructure.persistence.entities.DealershipEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DealershipRepository extends JpaRepository<DealershipEntity, Long> {
    void deleteByName(String name);

    DealershipEntity findByName(String name);
}