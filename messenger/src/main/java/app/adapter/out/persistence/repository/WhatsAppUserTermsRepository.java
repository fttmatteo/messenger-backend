package app.adapter.out.persistence.repository;

import app.adapter.out.persistence.entities.WhatsAppUserTermsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WhatsAppUserTermsRepository extends JpaRepository<WhatsAppUserTermsEntity, Long> {
    Optional<WhatsAppUserTermsEntity> findByPhoneNumber(String phoneNumber);
    boolean existsByPhoneNumber(String phoneNumber);
}
