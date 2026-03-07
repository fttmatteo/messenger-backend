package app.domain.services;

import app.domain.model.Dealership;
import app.domain.model.WhatsAppSession;
import app.domain.ports.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class WhatsAppBotServiceTest {

    @Mock
    private WhatsAppMessagePort messagePort;
    @Mock
    private WhatsAppSessionPort sessionPort;
    @Mock
    private SearchServiceDelivery searchService;
    @Mock
    private LocationPort locationPort;
    @Mock
    private StoragePort storagePort;
    @Mock
    private WhatsAppRateLimitPort rateLimitPort;

    @InjectMocks
    private WhatsAppBotService botService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    /**
     * Verifica que el bot procese el mensaje y espere el PIN.
     */
    void testProcessMessage_AwaitingPin_Success() {
        String from = "123456789";
        String pin = "1234";
        Dealership dealership = new Dealership();
        dealership.setIdDealership(1L);
        dealership.setName("Test Dealer");

        when(sessionPort.findActiveSession(from)).thenReturn(Optional.empty());
        when(rateLimitPort.isBlocked(from)).thenReturn(false);
        when(sessionPort.findDealershipByPin(pin)).thenReturn(Optional.of(dealership));
        when(sessionPort.createSession(anyString(), any(), anyInt())).thenReturn(new WhatsAppSession());

        botService.processMessage(from, "Hola");
        verify(messagePort).sendTextMessage(eq(from), contains("Ingresa el PIN"));

        botService.processMessage(from, pin);

        verify(rateLimitPort).clearFailedAttempts(from);
        verify(sessionPort).createSession(eq(from), eq(dealership), anyInt());
    }

    @Test
    /**
     * Verifica que el bot procese el mensaje y espere el PIN.
     */
    void testProcessMessage_AwaitingPin_Failure_NotBlocked() {
        String from = "123456789";
        String wrongPin = "0000";

        when(sessionPort.findActiveSession(from)).thenReturn(Optional.empty());
        when(rateLimitPort.isBlocked(from)).thenReturn(false);
        when(sessionPort.findDealershipByPin(wrongPin)).thenReturn(Optional.empty());
        when(rateLimitPort.recordFailedAttempt(from)).thenReturn(2);

        botService.processMessage(from, "Hola");
        botService.processMessage(from, wrongPin);

        verify(rateLimitPort).recordFailedAttempt(from);
        verify(messagePort).sendTextMessage(eq(from), contains("Intento 1 de 3"));
    }

    @Test
    /**
     * Verifica que el bot procese el mensaje y espere el PIN.
     */
    void testProcessMessage_AwaitingPin_Failure_FinalAttemptBlocks() {
        String from = "123456789";
        String wrongPin = "0000";

        when(sessionPort.findActiveSession(from)).thenReturn(Optional.empty());
        when(rateLimitPort.isBlocked(from)).thenReturn(false);
        when(sessionPort.findDealershipByPin(wrongPin)).thenReturn(Optional.empty());
        when(rateLimitPort.recordFailedAttempt(from)).thenReturn(0);

        botService.processMessage(from, "Hola");
        botService.processMessage(from, wrongPin);

        verify(messagePort).sendTextMessage(eq(from), contains("máximo de intentos"));
    }

    @Test
    /**
     * Verifica que el bot procese el mensaje y espere el PIN.
     */
    void testProcessMessage_AlreadyBlocked() {
        String from = "123456789";

        when(sessionPort.findActiveSession(from)).thenReturn(Optional.empty());
        when(rateLimitPort.isBlocked(from)).thenReturn(true);

        botService.processMessage(from, "any message");

        verify(messagePort).sendTextMessage(eq(from), contains("superado el límite de intentos"));
        verify(sessionPort, never()).findDealershipByPin(anyString());
    }
}
