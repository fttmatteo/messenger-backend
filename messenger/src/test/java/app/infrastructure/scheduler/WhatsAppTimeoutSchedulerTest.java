package app.infrastructure.scheduler;

import app.domain.model.WhatsAppSession;
import app.domain.ports.WhatsAppMessagePort;
import app.domain.ports.WhatsAppSessionPort;
import app.infrastructure.persistence.repository.WhatsAppSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class WhatsAppTimeoutSchedulerTest {

    private WhatsAppSessionPort sessionPort;
    private WhatsAppMessagePort messagePort;
    private WhatsAppSessionRepository sessionRepository;
    private WhatsAppTimeoutScheduler scheduler;

    @BeforeEach
    void setUp() {
        sessionPort = mock(WhatsAppSessionPort.class);
        messagePort = mock(WhatsAppMessagePort.class);
        sessionRepository = mock(WhatsAppSessionRepository.class);
        scheduler = new WhatsAppTimeoutScheduler(sessionPort, messagePort, sessionRepository);
    }

    @Test
    /**
     * Verifica que el planificador envíe mensajes y actualice las sesiones
     * correctamente.
     */
    void testCheckInactivityTimeouts_SendsMessagesAndUpdatesSessions() {
        WhatsAppSession session1 = new WhatsAppSession();
        session1.setPhoneNumber("123456789");
        session1.setTimeoutNotified(false);

        WhatsAppSession session2 = new WhatsAppSession();
        session2.setPhoneNumber("987654321");
        session2.setTimeoutNotified(false);

        when(sessionPort.findInactiveSessions(any(LocalDateTime.class)))
                .thenReturn(List.of(session1, session2));

        scheduler.checkInactivityTimeouts();

        verify(messagePort, times(2)).sendTextMessage(anyString(), contains("inactividad"));
        verify(messagePort).sendTextMessage(eq("123456789"), anyString());
        verify(messagePort).sendTextMessage(eq("987654321"), anyString());

        ArgumentCaptor<WhatsAppSession> sessionCaptor = ArgumentCaptor.forClass(WhatsAppSession.class);
        verify(sessionPort, times(2)).updateSession(sessionCaptor.capture());

        List<WhatsAppSession> updatedSessions = sessionCaptor.getAllValues();
        assertTrue(updatedSessions.get(0).isTimeoutNotified());
        assertTrue(updatedSessions.get(1).isTimeoutNotified());
    }

    @Test
    /**
     * Verifica que el planificador no realice ninguna acción cuando no hay
     * sesiones inactivas.
     */
    void testCheckInactivityTimeouts_NoSessions_DoesNothing() {

        when(sessionPort.findInactiveSessions(any(LocalDateTime.class)))
                .thenReturn(List.of());

        scheduler.checkInactivityTimeouts();

        verify(messagePort, never()).sendTextMessage(anyString(), anyString());
        verify(sessionPort, never()).updateSession(any());
    }

    @Test
    /**
     * Verifica que el planificador elimine correctamente las sesiones expiradas.
     */
    void testCleanupExpiredSessions_DeletesSessions() {
        when(sessionRepository.deleteExpiredSessions(any(LocalDateTime.class)))
                .thenReturn(5);

        scheduler.cleanupExpiredSessions();

        verify(sessionRepository).deleteExpiredSessions(any(LocalDateTime.class));
    }

    @Test
    /**
     * Verifica que el planificador no realice ninguna acción cuando no hay
     * sesiones expiradas para eliminar.
     */
    void testCleanupExpiredSessions_NothingToDelete() {
        when(sessionRepository.deleteExpiredSessions(any(LocalDateTime.class)))
                .thenReturn(0);

        scheduler.cleanupExpiredSessions();

        verify(sessionRepository).deleteExpiredSessions(any(LocalDateTime.class));
    }
}
