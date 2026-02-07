package app.infrastructure.persistence.repository;

import app.infrastructure.persistence.entities.WhatsAppSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repositorio para gestión de sesiones de WhatsApp.
 */
@Repository
public interface WhatsAppSessionRepository extends JpaRepository<WhatsAppSessionEntity, Long> {

    /**
     * Busca una sesión activa (no expirada) por número de teléfono.
     */
    Optional<WhatsAppSessionEntity> findByPhoneNumberAndExpiresAtAfter(String phoneNumber, LocalDateTime now);

    /**
     * Elimina sesiones expiradas (limpieza programada).
     */
    @Modifying
    @Query("DELETE FROM WhatsAppSessionEntity s WHERE s.expiresAt < :now")
    int deleteExpiredSessions(LocalDateTime now);

    /**
     * Elimina todas las sesiones de un número de teléfono.
     */
    void deleteByPhoneNumber(String phoneNumber);
}
