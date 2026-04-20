package app.infrastructure.scheduler;

import app.domain.model.WhatsAppSession;
import app.domain.ports.WhatsAppMessagePort;
import app.domain.ports.WhatsAppSessionPort;
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
    private final WhatsAppMessagePort messagePort;
    private final WhatsAppSessionRepository sessionRepository;

    public WhatsAppTimeoutScheduler(WhatsAppSessionPort sessionPort, WhatsAppMessagePort messagePort,
            WhatsAppSessionRepository sessionRepository) {
        this.sessionPort = sessionPort;
        this.messagePort = messagePort;
        this.sessionRepository = sessionRepository;
    }

    /**
     * Revisa sesiones inactivas cada minuto.
     */
    @Scheduled(fixedDelay = 60000) // Se ejecuta cada minuto
    @SchedulerLock(name = "wa_timeout_lock", lockAtMostFor = "PT50S", lockAtLeastFor = "PT30S")
    public void checkInactivityTimeouts() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(TIMEOUT_MINUTES);
        List<WhatsAppSession> inactiveSessions = sessionPort.findInactiveSessions(threshold);

        if (inactiveSessions.isEmpty()) {
            return;
        }

        logger.info("[Scheduler] Procesando {} sesiones inactivas para timeout.", inactiveSessions.size());

        for (WhatsAppSession session : inactiveSessions) {
            try {
                // Notificar al usuario
                messagePort.sendTextMessage(session.getPhoneNumber(),
                        "⏰ Por inactividad, hemos finalizado el chat. Si necesitas realizar una nueva consulta, ¡escríbeme! 👋");

                // Marcar como notificado para no repetir en la siguiente ejecución
                session.setTimeoutNotified(true);
                sessionPort.updateSession(session);

                logger.debug("[Scheduler] Timeout enviado a {}", maskPhone(session.getPhoneNumber()));
            } catch (Exception e) {
                logger.error("[Scheduler] Error procesando timeout para {}: {}", session.getPhoneNumber(),
                        e.getMessage());
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

    private String maskPhone(String phone) {
        if (phone == null || phone.length() <= 4) {
            return phone;
        }
        return "****" + phone.substring(phone.length() - 4);
    }
}
