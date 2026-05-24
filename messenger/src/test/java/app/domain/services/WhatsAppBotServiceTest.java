package app.domain.services;
import app.domain.ports.ServiceDeliveryPort;

import app.domain.model.Dealership;
import app.domain.model.WhatsAppSession;
import app.domain.ports.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Pruebas unitarias de WhatsAppBotService")
class WhatsAppBotServiceTest {

    @Mock
    private WhatsAppMessagePort messagePort;
    @Mock
    private WhatsAppSessionPort sessionPort;
    @Mock
    private ServiceDeliveryPort searchService;
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
    @DisplayName("Debe procesar mensaje en espera de PIN exitosamente")
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
    @DisplayName("Debe procesar mensaje en espera de PIN con falla sin bloquear")
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
    @DisplayName("Debe procesar mensaje en espera de PIN con falla y bloquear en último intento")
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
    @DisplayName("Debe procesar opción 1 de menú cambiando a espera de chasis")
    void testProcessMessage_MenuOption1_SwitchToAwaitingPlate() {
        String from = "123456789";
        Dealership dealership = new Dealership();
        dealership.setIdDealership(1L);
        dealership.setName("Test Dealer");

        WhatsAppSession session = new WhatsAppSession();
        session.setDealership(dealership);
        session.setConversationState(app.domain.model.enums.WhatsAppConversationState.MENU);

        when(sessionPort.findActiveSession(from)).thenReturn(Optional.of(session));

        botService.processMessage(from, "1");

        verify(sessionPort, atLeastOnce()).updateSession(argThat(s -> s.getConversationState() == app.domain.model.enums.WhatsAppConversationState.AWAITING_PLATE));
        verify(messagePort).sendTextMessage(eq(from), contains("Escribe el número del chasis"));
    }

    @Test
    @DisplayName("Debe procesar búsqueda directa en menú cuando parece placa o chasis")
    void testProcessMessage_Menu_LooksLikePlate_DirectSearch() {
        String from = "123456789";
        String chasis = "ABC12345674567";
        Dealership dealership = new Dealership();
        dealership.setIdDealership(1L);
        dealership.setName("Test Dealer");

        WhatsAppSession session = new WhatsAppSession();
        session.setDealership(dealership);
        session.setConversationState(app.domain.model.enums.WhatsAppConversationState.MENU);

        when(sessionPort.findActiveSession(from)).thenReturn(Optional.of(session));
        when(searchService.findByPlateAndDealershipPaginated(eq(chasis), anyLong(), any()))
                .thenReturn(org.springframework.data.domain.Page.empty());

        botService.processMessage(from, chasis);

        verify(searchService).findByPlateAndDealershipPaginated(eq(chasis), eq(1L), any());
        verify(messagePort).sendTextMessage(eq(from), contains("No se encontró el chasis"));
    }

    @Test
    @DisplayName("Debe procesar opción 0 de menú cerrando la sesión")
    void testProcessMessage_MenuOption0_CloseSession() {
        String from = "123456789";
        Dealership dealership = new Dealership();
        dealership.setIdDealership(1L);

        WhatsAppSession session = new WhatsAppSession();
        session.setDealership(dealership);
        session.setConversationState(app.domain.model.enums.WhatsAppConversationState.MENU);

        when(sessionPort.findActiveSession(from)).thenReturn(Optional.of(session));

        botService.processMessage(from, "0");

        verify(sessionPort).deleteByPhoneNumber(from);
        verify(messagePort).sendTextMessage(eq(from), contains("Sesión cerrada correctamente"));
    }

    @Test
    @DisplayName("Debe procesar siguiente página de menú")
    void testProcessMessage_Menu_NextPage() {
        String from = "123456789";
        Dealership dealership = new Dealership();
        dealership.setIdDealership(1L);

        WhatsAppSession session = new WhatsAppSession();
        session.setDealership(dealership);
        session.setConversationState(app.domain.model.enums.WhatsAppConversationState.MENU);
        session.setCurrentPage(0);
        session.setLastFilterStatuses("ASSIGNED");

        when(sessionPort.findActiveSession(from)).thenReturn(Optional.of(session));
        when(searchService.findByDealershipIdAndStatusesPaginated(anyLong(), anyList(), any())).thenReturn(org.springframework.data.domain.Page.empty());

        botService.processMessage(from, "NEXT_PAGE");

        verify(sessionPort, atLeastOnce()).updateSession(argThat(s -> s.getCurrentPage() == 1));
    }

    @Test
    @DisplayName("Debe procesar sesión ya bloqueada")
    void testProcessMessage_AlreadyBlocked() {
        String from = "123456789";

        when(sessionPort.findActiveSession(from)).thenReturn(Optional.empty());
        when(rateLimitPort.isBlocked(from)).thenReturn(true);

        botService.processMessage(from, "any message");

        verify(messagePort).sendTextMessage(eq(from), contains("superado el límite de intentos"));
        verify(sessionPort, never()).findDealershipByPin(anyString());
    }
}
