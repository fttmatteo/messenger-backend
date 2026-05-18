package app.infrastructure.scheduler;

import app.domain.model.WhatsAppSession;
import app.domain.ports.WhatsAppMessagePort;
import app.domain.ports.WhatsAppSessionPort;
import app.infrastructure.persistence.repository.WhatsAppSessionRepository;
import app.domain.model.enums.WhatsAppConversationState;
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
    @DisplayName("Debe verificar tiempos de espera por inactividad reiniciando sesiones de forma silenciosa")
    void testCheckInactivityTimeouts_ResetsSessionsSilently() {
        WhatsAppSession session1 = new WhatsAppSession();
        session1.setPhoneNumber("123456789");
        session1.setTimeoutNotified(false);
        session1.setConversationState(WhatsAppConversationState.AWAITING_PLATE);
        session1.setCurrentPage(3);
        session1.setLastFilterStatuses("ASSIGNED");

        WhatsAppSession session2 = new WhatsAppSession();
        session2.setPhoneNumber("987654321");
        session2.setTimeoutNotified(false);
        session2.setConversationState(WhatsAppConversationState.MENU);
        session2.setCurrentPage(0);

        when(sessionPort.findInactiveSessions(any(LocalDateTime.class)))
                .thenReturn(List.of(session1, session2));

        scheduler.checkInactivityTimeouts();

        verify(messagePort, never()).sendTextMessage(anyString(), anyString());

        ArgumentCaptor<WhatsAppSession> sessionCaptor = ArgumentCaptor.forClass(WhatsAppSession.class);
        verify(sessionPort, times(2)).updateSession(sessionCaptor.capture());

        List<WhatsAppSession> updatedSessions = sessionCaptor.getAllValues();

        assertEquals(WhatsAppConversationState.MENU, updatedSessions.get(0).getConversationState());
        assertEquals(0, updatedSessions.get(0).getCurrentPage());
        assertNull(updatedSessions.get(0).getLastFilterStatuses());
        assertTrue(updatedSessions.get(0).isTimeoutNotified());

        assertEquals(WhatsAppConversationState.MENU, updatedSessions.get(1).getConversationState());
        assertEquals(0, updatedSessions.get(1).getCurrentPage());
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
