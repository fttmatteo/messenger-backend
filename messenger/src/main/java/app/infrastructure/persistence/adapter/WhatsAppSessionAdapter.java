package app.infrastructure.persistence.adapter;

import app.domain.model.Dealership;
import app.domain.model.WhatsAppSession;
import app.domain.ports.WhatsAppSessionPort;
import app.infrastructure.config.WhatsAppConfig;
import app.infrastructure.persistence.entities.DealershipEntity;
import app.infrastructure.persistence.entities.WhatsAppSessionEntity;
import app.infrastructure.persistence.repository.DealershipRepository;
import app.infrastructure.persistence.repository.WhatsAppSessionRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Adaptador de persistencia para sesiones de WhatsApp.
 * Implementa el puerto del dominio.
 */
@Component
public class WhatsAppSessionAdapter implements WhatsAppSessionPort {

    private final WhatsAppSessionRepository sessionRepository;
    private final DealershipRepository dealershipRepository;
    private final WhatsAppConfig config;

    public WhatsAppSessionAdapter(
            WhatsAppSessionRepository sessionRepository,
            DealershipRepository dealershipRepository,
            WhatsAppConfig config) {
        this.sessionRepository = sessionRepository;
        this.dealershipRepository = dealershipRepository;
        this.config = config;
    }

    @Override
    public Optional<WhatsAppSession> findActiveSession(String phoneNumber) {
        return sessionRepository
                .findByPhoneNumberAndExpiresAtAfter(phoneNumber, LocalDateTime.now())
                .map(this::toDomain);
    }

    @Override
    @Transactional
    public WhatsAppSession createSession(String phoneNumber, Dealership dealership, int expirationHours) {
        // Eliminar sesiones anteriores
        sessionRepository.deleteByPhoneNumber(phoneNumber);

        // Buscar la entidad del dealership
        DealershipEntity dealershipEntity = dealershipRepository.findById(dealership.getIdDealership())
                .orElseThrow(() -> new IllegalArgumentException("Dealership not found"));

        WhatsAppSessionEntity entity = new WhatsAppSessionEntity();
        entity.setPhoneNumber(phoneNumber);
        entity.setDealership(dealershipEntity);
        entity.setExpiresAt(LocalDateTime.now().plusHours(expirationHours));

        WhatsAppSessionEntity saved = sessionRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    @Transactional
    public void deleteByPhoneNumber(String phoneNumber) {
        sessionRepository.deleteByPhoneNumber(phoneNumber);
    }

    @Override
    public Optional<Dealership> findDealershipByPin(String pin) {
        return dealershipRepository.findByWhatsappPin(pin)
                .map(this::dealershipToDomain);
    }

    @Override
    public int getSessionExpirationHours() {
        return config.getSessionExpirationHours();
    }

    private WhatsAppSession toDomain(WhatsAppSessionEntity entity) {
        WhatsAppSession session = new WhatsAppSession();
        session.setId(entity.getId());
        session.setPhoneNumber(entity.getPhoneNumber());
        session.setDealership(dealershipToDomain(entity.getDealership()));
        session.setExpiresAt(entity.getExpiresAt());
        session.setCreatedAt(entity.getCreatedAt());
        return session;
    }

    private Dealership dealershipToDomain(DealershipEntity entity) {
        Dealership d = new Dealership();
        d.setIdDealership(entity.getIdDealership());
        d.setName(entity.getName());
        d.setAddress(entity.getAddress());
        d.setPhone(entity.getPhone());
        d.setZone(entity.getZone());
        d.setLatitude(entity.getLatitude());
        d.setLongitude(entity.getLongitude());
        d.setIsGeolocated(entity.getIsGeolocated());
        d.setWhatsappPin(entity.getWhatsappPin());
        return d;
    }
}
