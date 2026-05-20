package app.infrastructure.scheduler;

import app.domain.model.WhatsAppSession;
import app.domain.ports.WhatsAppMessagePort;
import app.domain.ports.WhatsAppSessionPort;
import app.domain.model.enums.WhatsAppConversationState;
import app.domain.util.LogSanitizer;
import app.infrastructure.persistence.repository.WhatsAppSessionRepository;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Programador para manejar los tiempos de inactividad de WhatsApp de forma
 * distribuida.
 * Se ejecuta cada minuto y busca sesiones que no han tenido actividad reciente.
 */
@Component
public class WhatsAppTimeoutScheduler {

    private static final Logger logger = LoggerFactory.getLogger(WhatsAppTimeoutScheduler.class);
    private static final int TIMEOUT_MINUTES = 5;

    private final WhatsAppSessionPort sessionPort;
    private final WhatsAppSessionRepository sessionRepository;

    public WhatsAppTimeoutScheduler(WhatsAppSessionPort sessionPort, WhatsAppMessagePort messagePort,
            WhatsAppSessionRepository sessionRepository) {
        this.sessionPort = sessionPort;
        this.sessionRepository = sessionRepository;
    }

    /**
     * Revisa sesiones inactivas cada minuto.
     */
    @Scheduled(fixedDelay = 60000)
    @SchedulerLock(name = "wa_timeout_lock", lockAtMostFor = "PT50S", lockAtLeastFor = "PT30S")
    public void checkInactivityTimeouts() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(TIMEOUT_MINUTES);
        List<WhatsAppSession> inactiveSessions = sessionPort.findInactiveSessions(threshold);

        if (inactiveSessions.isEmpty()) {
            return;
        }
        

        for (WhatsAppSession session : inactiveSessions) {
            try {
                session.setConversationState(WhatsAppConversationState.MENU);
                session.setCurrentPage(0);
                session.setLastFilterStatuses(null);
                session.setTimeoutNotified(true);
                sessionPort.updateSession(session);

                logger.debug("[Scheduler] Sesión de {} reseteada silenciosamente por inactividad.", LogSanitizer.maskGeneric(session.getPhoneNumber(), 4));
            } catch (Exception e) {
                logger.error("[Scheduler] Error procesando timeout silencioso para sessionId={} phone={}: {}",
                        session.getId(), LogSanitizer.maskGeneric(session.getPhoneNumber(), 4), e.getMessage(), e);
            }
        }
    }

    /**
     * Limpia sesiones expiradas diariamente a las 4:00 AM.
     */
    @Scheduled(cron = "0 0 4 * * ?")
    @Transactional
    @SchedulerLock(name = "wa_session_cleanup_lock", lockAtMostFor = "PT10M", lockAtLeastFor = "PT5M")
    public void cleanupExpiredSessions() {
        int deleted = sessionRepository.deleteExpiredSessions(LocalDateTime.now());
        if (deleted > 0) {
            logger.info("[Scheduler] {} sesiones de WhatsApp expiradas eliminadas.", deleted);
        }
    }


}
