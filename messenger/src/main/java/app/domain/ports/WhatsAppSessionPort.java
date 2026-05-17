package app.domain.ports;

import app.domain.model.Dealership;
import app.domain.model.WhatsAppSession;
import java.util.Optional;

/**
 * Puerto de salida para gestión de sesiones de WhatsApp.
 * Desacopla el dominio de la persistencia.
 */
public interface WhatsAppSessionPort {
    Optional<WhatsAppSession> findActiveSession(String phoneNumber);

    WhatsAppSession createSession(String phoneNumber, Dealership dealership, int expirationHours);

    void deleteByPhoneNumber(String phoneNumber);

    Optional<Dealership> findDealershipByPin(String pin);

    int getSessionExpirationHours();

    boolean isMasterPin(String pin);

    void updateSession(WhatsAppSession session);

    java.util.List<WhatsAppSession> findActiveSessionsByDealership(Long dealershipId);

    java.util.List<WhatsAppSession> findInactiveSessions(java.time.LocalDateTime threshold);
}
