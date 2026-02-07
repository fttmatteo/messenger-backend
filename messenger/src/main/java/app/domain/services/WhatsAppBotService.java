package app.domain.services;

import app.domain.model.Dealership;
import app.domain.model.ServiceDelivery;
import app.domain.model.StatusHistory;
import app.domain.model.WhatsAppSession;
import app.domain.model.enums.Status;
import app.domain.ports.WhatsAppMessagePort;
import app.domain.ports.WhatsAppSessionPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servicio principal del bot de WhatsApp.
 * Maneja autenticación por PIN y consultas de placas.
 * Usa ports para desacoplarse de la infraestructura (arquitectura hexagonal).
 */
@Service
public class WhatsAppBotService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final WhatsAppMessagePort messagePort;
    private final WhatsAppSessionPort sessionPort;
    private final SearchServiceDelivery searchService;

    // Estado temporal para flujo de conversación
    private final Map<String, ConversationState> conversationStates = new ConcurrentHashMap<>();

    private enum ConversationState {
        AWAITING_PIN, AWAITING_PLATE, MENU
    }

    public WhatsAppBotService(
            WhatsAppMessagePort messagePort,
            WhatsAppSessionPort sessionPort,
            SearchServiceDelivery searchService) {
        this.messagePort = messagePort;
        this.sessionPort = sessionPort;
        this.searchService = searchService;
    }

    /**
     * Procesa un mensaje entrante de WhatsApp.
     */
    @Transactional
    public void processMessage(String from, String messageBody) {
        String text = messageBody.trim();

        // 1. Verificar si tiene sesión activa
        Optional<WhatsAppSession> sessionOpt = sessionPort.findActiveSession(from);

        if (sessionOpt.isPresent()) {
            handleAuthenticatedUser(from, text, sessionOpt.get());
        } else {
            handleUnauthenticatedUser(from, text);
        }
    }

    private void handleUnauthenticatedUser(String from, String text) {
        ConversationState state = conversationStates.get(from);

        if (state == ConversationState.AWAITING_PIN) {
            // Intentar autenticar con el PIN
            Optional<Dealership> dealershipOpt = sessionPort.findDealershipByPin(text);

            if (dealershipOpt.isPresent()) {
                Dealership dealership = dealershipOpt.get();
                sessionPort.createSession(from, dealership, sessionPort.getSessionExpirationHours());
                conversationStates.put(from, ConversationState.MENU);
                sendMenu(from, dealership.getName());
            } else {
                messagePort.sendTextMessage(from, "❌ PIN incorrecto. Intenta de nuevo:");
            }
        } else {
            // Solicitar PIN
            conversationStates.put(from, ConversationState.AWAITING_PIN);
            messagePort.sendTextMessage(from,
                    "Hola, 🔒 Ingresa el PIN de 4 digitos proporcionado por tu concesionario para continuar:");
        }
    }

    private void handleAuthenticatedUser(String from, String text, WhatsAppSession session) {
        ConversationState state = conversationStates.getOrDefault(from, ConversationState.MENU);
        Long dealershipId = session.getDealership().getIdDealership();
        String dealershipName = session.getDealership().getName();

        switch (state) {
            case AWAITING_PLATE -> {
                List<ServiceDelivery> services = searchService.findByPlateAndDealership(text, dealershipId);
                if (services.isEmpty()) {
                    messagePort.sendTextMessage(from,
                            "❌ No se encontró la placa *" + text.toUpperCase() + "* en " + dealershipName);
                } else {
                    sendPlateDetails(from, services);
                }
                conversationStates.put(from, ConversationState.MENU);
                sendMenu(from, dealershipName);
            }
            case MENU -> {
                switch (text) {
                    case "1" -> {
                        conversationStates.put(from, ConversationState.AWAITING_PLATE);
                        messagePort.sendTextMessage(from, "Escribe el número de placa:");
                    }
                    case "2" -> {
                        sendPendingList(from, dealershipId, dealershipName);
                        sendMenu(from, dealershipName);
                    }
                    default -> {
                        // Si parece una placa, buscarla directamente
                        if (looksLikePlate(text)) {
                            List<ServiceDelivery> services = searchService.findByPlateAndDealership(text, dealershipId);
                            if (!services.isEmpty()) {
                                sendPlateDetails(from, services);
                                sendMenu(from, dealershipName);
                            } else {
                                messagePort.sendTextMessage(from,
                                        "❌ No se encontró la placa *" + text.toUpperCase() + "* en " + dealershipName);
                                sendMenu(from, dealershipName);
                            }
                        } else {
                            sendMenu(from, dealershipName);
                        }
                    }
                }
            }
            default -> sendMenu(from, dealershipName);
        }
    }

    private void sendMenu(String from, String dealershipName) {
        String menu = String.format(
                "✅ *%s*\n\n📋 *¿Qué deseas consultar?*\n- 1️⃣ Buscar una placa específica\n- 2️⃣ Ver todas las placas asignadas",
                dealershipName);
        messagePort.sendTextMessage(from, menu);
    }

    private void sendPlateDetails(String from, List<ServiceDelivery> services) {
        StringBuilder sb = new StringBuilder();
        for (ServiceDelivery s : services) {
            sb.append(formatServiceDetail(s));
        }
        messagePort.sendTextMessage(from, sb.toString().trim());
    }

    private String formatServiceDetail(ServiceDelivery s) {
        String statusEmoji = getStatusEmoji(s.getCurrentStatus());
        String statusName = getStatusName(s.getCurrentStatus());

        // Buscar ubicación en el historial para el estado actual
        String locationInfo = "";
        if (s.getHistory() != null && !s.getHistory().isEmpty()) {
            Optional<StatusHistory> lastUpdate = s.getHistory().stream()
                    .filter(h -> h.getNewStatus() == s.getCurrentStatus())
                    .max((h1, h2) -> h1.getChangeDate().compareTo(h2.getChangeDate()));

            if (lastUpdate.isPresent() && lastUpdate.get().getDeliveryLatitude() != null
                    && lastUpdate.get().getDeliveryLongitude() != null) {
                locationInfo = String.format("\n📍 *Ubicación:* https://www.google.com/maps?q=%f,%f",
                        lastUpdate.get().getDeliveryLatitude(),
                        lastUpdate.get().getDeliveryLongitude());
            }
        }

        return String.format(
                "🚗 *Placa: %s*\n*Estado:* %s %s\n*Mensajero:* %s\n*Concesionario:* %s\n*Fecha:* %s%s\n\n",
                s.getPlate().getPlateNumber(),
                statusEmoji,
                statusName,
                s.getMessenger() != null ? s.getMessenger().getFullName() : "Sin asignar",
                s.getDealership().getName(),
                s.getCreatedAt() != null ? s.getCreatedAt().format(DATE_FORMAT) : "No disponible",
                locationInfo);
    }

    private String getStatusName(Status status) {
        return switch (status) {
            case PENDING -> "PENDIENTE";
            case ASSIGNED -> "ASIGNADO";
            case DELIVERED -> "ENTREGADO";
            case RETURNED -> "DEVUELTO";
            case CANCELED -> "CANCELADO";
            case RESOLVED -> "RESUELTO";
            case FAILED -> "FALLIDO";
            case DELETED -> "ELIMINADO";
        };
    }

    private void sendPendingList(String from, Long dealershipId, String dealershipName) {
        List<ServiceDelivery> pending = searchService.findPendingByDealership(dealershipId);

        if (pending.isEmpty()) {
            messagePort.sendTextMessage(from, "✅ Todavia no hay placa(s) asignada(s) para " + dealershipName);
            return;
        }

        StringBuilder sb = new StringBuilder("📦 *Placas asignada(s) a " + dealershipName + "*\n\n");
        for (ServiceDelivery s : pending) {
            sb.append(formatServiceDetail(s));
        }
        messagePort.sendTextMessage(from, sb.toString().trim());
    }

    private String getStatusEmoji(Status status) {
        return switch (status) {
            case PENDING -> "⏳";
            case ASSIGNED -> "📝";
            case DELIVERED -> "✅";
            case RETURNED -> "↩️";
            case CANCELED -> "❌";
            case RESOLVED -> "🔧";
            case FAILED -> "⚠️";
            case DELETED -> "🗑️";
        };
    }

    private boolean looksLikePlate(String text) {
        // Acepta los 3 formatos del sistema:
        return text.matches("(?i)^[A-Z]{3}-?\\d{3}$") ||
                text.matches("(?i)^\\d{3}-?[A-Z]{3}$") ||
                text.matches("(?i)^[A-Z]{3}-?\\d{2}[A-Z]$");
    }
}
