package app.infrastructure.scheduler;

import app.domain.model.WhatsAppSession;
import app.domain.ports.WhatsAppMessagePort;
import app.domain.ports.WhatsAppSessionPort;
import app.infrastructure.persistence.repository.WhatsAppSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("Pruebas unitarias de WhatsAppTimeoutScheduler")
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
    @DisplayName("Debe verificar tiempos de espera por inactividad enviando mensajes y actualizando sesiones")
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
    @DisplayName("No debe hacer nada si no hay sesiones por inactividad")
    void testCheckInactivityTimeouts_NoSessions_DoesNothing() {

        when(sessionPort.findInactiveSessions(any(LocalDateTime.class)))
                .thenReturn(List.of());

        scheduler.checkInactivityTimeouts();

        verify(messagePort, never()).sendTextMessage(anyString(), anyString());
        verify(sessionPort, never()).updateSession(any());
    }

    @Test
    @DisplayName("Debe limpiar sesiones expiradas eliminándolas")
    void testCleanupExpiredSessions_DeletesSessions() {
        when(sessionRepository.deleteExpiredSessions(any(LocalDateTime.class)))
                .thenReturn(5);

        scheduler.cleanupExpiredSessions();

        verify(sessionRepository).deleteExpiredSessions(any(LocalDateTime.class));
    }

    @Test
    @DisplayName("No debe hacer nada si no hay sesiones expiradas que limpiar")
    void testCleanupExpiredSessions_NothingToDelete() {
        when(sessionRepository.deleteExpiredSessions(any(LocalDateTime.class)))
                .thenReturn(0);

        scheduler.cleanupExpiredSessions();

        verify(sessionRepository).deleteExpiredSessions(any(LocalDateTime.class));
    }
}
