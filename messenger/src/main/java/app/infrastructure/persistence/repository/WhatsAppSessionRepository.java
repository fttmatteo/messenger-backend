package app.infrastructure.persistence.repository;

import app.infrastructure.persistence.entities.WhatsAppSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
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
     * Busca todas las sesiones activas (no expiradas) por ID de concesionario.
     */
    List<WhatsAppSessionEntity> findByDealership_IdDealershipAndExpiresAtAfter(Long dealershipId, LocalDateTime now);

    /**
     * Elimina sesiones expiradas (limpieza programada).
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM WhatsAppSessionEntity s WHERE s.expiresAt < :now")
    int deleteExpiredSessions(LocalDateTime now);

    /**
     * Busca sesiones que han superado el umbral de inactividad y no han sido
     * notificadas.
     */
    List<WhatsAppSessionEntity> findByExpiresAtAfterAndLastActivityAtBeforeAndTimeoutNotifiedFalse(LocalDateTime now,
            LocalDateTime threshold);

    /**
     * Elimina todas las sesiones de un número de teléfono.
     */
    void deleteByPhoneNumber(String phoneNumber);
}
