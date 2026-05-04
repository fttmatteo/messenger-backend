package app.infrastructure.persistence.repository;

import app.infrastructure.persistence.entities.SignatureEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SignatureRepository extends JpaRepository<SignatureEntity, Long> {
}
