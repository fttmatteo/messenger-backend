package app.infrastructure.persistence.repository;

import app.infrastructure.persistence.entities.WhatsAppSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para gestión de sesiones de WhatsApp.
 */
@Repository
public interface WhatsAppSessionRepository extends JpaRepository<WhatsAppSessionEntity, Long> {
    Optional<WhatsAppSessionEntity> findByPhoneNumberAndExpiresAtAfter(String phoneNumber, LocalDateTime now);

    List<WhatsAppSessionEntity> findByDealership_IdDealershipAndExpiresAtAfter(Long dealershipId, LocalDateTime now);

    @Modifying
    @Query("DELETE FROM WhatsAppSessionEntity s WHERE s.expiresAt < :now")
    int deleteExpiredSessions(LocalDateTime now);

    List<WhatsAppSessionEntity> findByExpiresAtAfterAndLastActivityAtBeforeAndTimeoutNotifiedFalse(LocalDateTime now,
            LocalDateTime threshold);

    void deleteByPhoneNumber(String phoneNumber);
}
