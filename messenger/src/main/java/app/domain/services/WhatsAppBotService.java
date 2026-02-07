package app.domain.services;

import app.domain.model.Dealership;
import app.domain.model.ServiceDelivery;
import app.domain.model.WhatsAppSession;
import app.domain.model.enums.Status;
import app.domain.ports.WhatsAppMessagePort;
import app.domain.ports.WhatsAppSessionPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(WhatsAppBotService.class);
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
        logger.info("Mensaje recibido de {}: {}", from, messageBody);
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
                logger.info("Sesión creada para {} - {}", from, dealership.getName());
            } else {
                messagePort.sendTextMessage(from, "❌ PIN incorrecto. Intenta de nuevo:");
            }
        } else {
            // Solicitar PIN
            conversationStates.put(from, ConversationState.AWAITING_PIN);
            messagePort.sendTextMessage(from, "🔒 Ingresa el PIN de tu concesionario:");
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
                sendMenuReminder(from);
            }
            case MENU -> {
                switch (text) {
                    case "1" -> {
                        conversationStates.put(from, ConversationState.AWAITING_PLATE);
                        messagePort.sendTextMessage(from, "Escribe el número de placa:");
                    }
                    case "2" -> {
                        sendPendingList(from, dealershipId, dealershipName);
                        sendMenuReminder(from);
                    }
                    case "0", "menu" -> sendMenu(from, dealershipName);
                    default -> {
                        // Si parece una placa, buscarla directamente
                        if (looksLikePlate(text)) {
                            List<ServiceDelivery> services = searchService.findByPlateAndDealership(text, dealershipId);
                            if (!services.isEmpty()) {
                                sendPlateDetails(from, services);
                                sendMenuReminder(from);
                            } else {
                                messagePort.sendTextMessage(from,
                                        "❌ Placa *" + text.toUpperCase() + "* no encontrada.\n\n" +
                                                "📋 *Menú*\n1️⃣ Buscar placa\n2️⃣ Ver pendientes");
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
                "✅ *%s*\n\n📋 *¿Qué deseas consultar?*\n1️⃣ Buscar placa específica\n2️⃣ Ver todas las placas pendientes",
                dealershipName);
        messagePort.sendTextMessage(from, menu);
    }

    private void sendMenuReminder(String from) {
        messagePort.sendTextMessage(from,
                "📋 Escribe *1* para buscar otra placa, *2* para ver pendientes, o *0* para ver el menú.");
    }

    private void sendPlateDetails(String from, List<ServiceDelivery> services) {
        StringBuilder sb = new StringBuilder();
        for (ServiceDelivery s : services) {
            String statusEmoji = getStatusEmoji(s.getCurrentStatus());
            sb.append(String.format(
                    "🚗 *Placa: %s*\n├ Estado: %s %s\n├ Mensajero: %s\n├ Concesionario: %s\n└ Fecha: %s\n\n",
                    s.getPlate().getPlateNumber(),
                    statusEmoji,
                    s.getCurrentStatus().name(),
                    s.getMessenger() != null ? s.getMessenger().getFullName() : "Sin asignar",
                    s.getDealership().getName(),
                    s.getCreatedAt().format(DATE_FORMAT)));
        }
        messagePort.sendTextMessage(from, sb.toString().trim());
    }

    private void sendPendingList(String from, Long dealershipId, String dealershipName) {
        List<ServiceDelivery> pending = searchService.findPendingByDealership(dealershipId);

        if (pending.isEmpty()) {
            messagePort.sendTextMessage(from, "✅ No hay placas pendientes en " + dealershipName);
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("📦 *Placas pendientes - ").append(dealershipName).append("*\n\n");

        int count = 1;
        for (ServiceDelivery s : pending) {
            String statusEmoji = getStatusEmoji(s.getCurrentStatus());
            sb.append(String.format("%d. %s - %s %s\n",
                    count++,
                    s.getPlate().getPlateNumber(),
                    statusEmoji,
                    s.getCurrentStatus().name()));
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
        // Placa colombiana: 3 letras + 3 números (o variantes)
        return text.matches("(?i)^[A-Z]{3}\\d{3}$") || text.matches("(?i)^[A-Z]{3}-?\\d{3}$");
    }
}
