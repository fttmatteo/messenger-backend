package app.adapter.out.persistence.adapter;

import app.domain.model.Dealership;
import app.domain.model.WhatsAppSession;
import app.domain.ports.WhatsAppSessionPort;
import app.infrastructure.config.WhatsAppConfig;
import app.adapter.out.persistence.entities.DealershipEntity;
import app.adapter.out.persistence.entities.WhatsAppSessionEntity;
import app.adapter.out.persistence.repository.DealershipRepository;
import app.adapter.out.persistence.repository.WhatsAppSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adaptador de persistencia para sesiones de WhatsApp.
 * Implementa el puerto del dominio.
 */
@Component
public class WhatsAppSessionAdapter implements WhatsAppSessionPort {

    private static final Logger logger = LoggerFactory.getLogger(WhatsAppSessionAdapter.class);
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
    @Transactional(readOnly = true)
    public Optional<WhatsAppSession> findActiveSession(String phoneNumber) {
        return sessionRepository
                .findByPhoneNumberAndExpiresAtAfter(phoneNumber, LocalDateTime.now())
                .map(this::toDomain);
    }

    @Override
    @Transactional
    public WhatsAppSession createSession(String phoneNumber, Dealership dealership, int expirationHours) {
        sessionRepository.deleteByPhoneNumber(phoneNumber);

        DealershipEntity dealershipEntity = null;
        if (dealership.getIdDealership() != null) {
            dealershipEntity = dealershipRepository.findById(dealership.getIdDealership())
                    .orElseThrow(() -> new IllegalArgumentException("Dealership not found"));
        }

        WhatsAppSessionEntity entity = new WhatsAppSessionEntity();
        entity.setPhoneNumber(phoneNumber);
        entity.setDealership(dealershipEntity);
        entity.setExpiresAt(LocalDateTime.now().plusHours(expirationHours));

        WhatsAppSessionEntity saved = sessionRepository.save(entity);
        logger.debug("[DB] Sesión creada/actualizada para {}", maskPhone(phoneNumber));
        return toDomain(saved);
    }

    @Override
    @Transactional
    public void deleteByPhoneNumber(String phoneNumber) {
        sessionRepository.deleteByPhoneNumber(phoneNumber);
        logger.debug("[DB] Sesión eliminada para {}", maskPhone(phoneNumber));
    }

    @Override
    @Transactional
    public void updateSession(WhatsAppSession session) {
        WhatsAppSessionEntity entity = sessionRepository.findById(session.getId())
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));

        entity.setCurrentPage(session.getCurrentPage());
        entity.setLastFilterStatuses(session.getLastFilterStatuses());
        entity.setExpiresAt(session.getExpiresAt());
        entity.setLastActivityAt(session.getLastActivityAt());
        entity.setTimeoutNotified(session.isTimeoutNotified());
        entity.setConversationState(session.getConversationState());

        sessionRepository.save(entity);
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() <= 4) {
            return phone;
        }
        return "****" + phone.substring(phone.length() - 4);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Dealership> findDealershipByPin(String pin) {
        return dealershipRepository.findByWhatsappPin(pin)
                .map(this::dealershipToDomain);
    }

    @Override
    public int getSessionExpirationHours() {
        return config.getSessionExpirationHours();
    }

    @Override
    public boolean isMasterPin(String pin) {
        return config.getMasterPin() != null && config.getMasterPin().equals(pin);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WhatsAppSession> findActiveSessionsByDealership(Long dealershipId) {
        return sessionRepository
                .findActiveSessionsByDealershipOrMaster(dealershipId, LocalDateTime.now())
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<WhatsAppSession> findInactiveSessions(LocalDateTime threshold) {
        return sessionRepository
                .findByExpiresAtAfterAndLastActivityAtBeforeAndTimeoutNotifiedFalse(LocalDateTime.now(), threshold)
                .stream()
                .map(entity -> toDomain(entity, false))
                .collect(Collectors.toList());
    }

    private WhatsAppSession toDomain(WhatsAppSessionEntity entity) {
        return toDomain(entity, true);
    }

    private WhatsAppSession toDomain(WhatsAppSessionEntity entity, boolean includeDealership) {
        WhatsAppSession session = new WhatsAppSession();
        session.setId(entity.getId());
        session.setPhoneNumber(entity.getPhoneNumber());
        if (includeDealership) {
            session.setDealership(dealershipToDomain(entity.getDealership()));
        }
        session.setExpiresAt(entity.getExpiresAt());
        session.setCreatedAt(entity.getCreatedAt());
        session.setCurrentPage(entity.getCurrentPage());
        session.setLastFilterStatuses(entity.getLastFilterStatuses());
        session.setLastActivityAt(entity.getLastActivityAt());
        session.setTimeoutNotified(entity.isTimeoutNotified());
        session.setConversationState(entity.getConversationState());
        return session;
    }

    private Dealership dealershipToDomain(DealershipEntity entity) {
        if (entity == null) {
            Dealership d = new Dealership();
            d.setIdDealership(null);
            d.setName("Llave Maestra");
            return d;
        }
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
