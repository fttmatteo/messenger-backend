package app.domain.services;

import app.domain.model.Dealership;
import app.domain.model.ServiceDelivery;
import app.domain.model.StatusHistory;
import app.domain.model.Location;
import app.domain.model.WhatsAppSession;
import app.domain.model.enums.Status;
import app.domain.ports.LocationPort;
import app.domain.ports.WhatsAppMessagePort;
import app.domain.ports.WhatsAppSessionPort;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.*;

/**
 * Servicio principal del bot de WhatsApp.
 * Maneja autenticación por PIN y consultas de placas.
 * Usa ports para desacoplarse de la infraestructura (arquitectura hexagonal).
 */
@Service
public class WhatsAppBotService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final Map<String, ScheduledFuture<?>> scheduledTimeouts = new ConcurrentHashMap<>();
    private final Set<String> timeoutNotified = ConcurrentHashMap.newKeySet();
    private final WhatsAppMessagePort messagePort;
    private final WhatsAppSessionPort sessionPort;
    private final SearchServiceDelivery searchService;
    private final LocationPort locationPort;
    private final Map<String, ConversationState> conversationStates = new ConcurrentHashMap<>();

    private enum ConversationState {
        AWAITING_PIN, AWAITING_PLATE, MENU
    }

    public WhatsAppBotService(
            WhatsAppMessagePort messagePort,
            WhatsAppSessionPort sessionPort,
            SearchServiceDelivery searchService,
            LocationPort locationPort) {
        this.messagePort = messagePort;
        this.sessionPort = sessionPort;
        this.searchService = searchService;
        this.locationPort = locationPort;
    }

    /**
     * Procesa un mensaje entrante de WhatsApp.
     */
    @Transactional
    public void processMessage(String from, String messageBody) {
        String text = messageBody.trim();

        // Cancelar cualquier timeout previo
        cancelTimeout(from);
        // Verificar si venimos de un estado de inactividad
        boolean wasTimedOut = timeoutNotified.remove(from);
        // Verificar sesión
        Optional<WhatsAppSession> sessionOpt = sessionPort.findActiveSession(from);

        if (wasTimedOut) {
            if (sessionOpt.isPresent()) {
                messagePort.sendTextMessage(from, "¡Hola de nuevo! 👋 ¿En qué puedo ayudarte hoy?.");
            } else {
                conversationStates.remove(from);
            }
        }

        if (sessionOpt.isPresent()) {
            handleAuthenticatedUser(from, text, sessionOpt.get());
        } else {
            handleUnauthenticatedUser(from, text);
        }

        // Programar nuevo timeout de 5 minutos
        scheduleTimeout(from);
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
            conversationStates.put(from, ConversationState.AWAITING_PIN);
            messagePort.sendTextMessage(from,
                    "🚦 *Tránsito de Sabaneta - Matriculas Iniciales*\n\n _Área de mensajería_\n\n ¡Hola! 👋. Aquí podrás consultar el estado de las placas programadas para entrega.\n\n"
                            + "`Nota: Por seguridad, el PIN se solicita cada 12 horas.`\n\n"
                            + "🔒 Ingresa el PIN de 4 dígitos proporcionado por tu concesionario para continuar:");
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
                            "⚠️ No se encontró la placa *" + text.toUpperCase() + "* en " + dealershipName + ".\n\n"
                                    + "Por favor, verifica que la placa sea correcta o consulte más tarde.");
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
                                        "⚠️ No se encontró la placa *" + text.toUpperCase() + "* en " + dealershipName
                                                + ".\n\n"
                                                + "Por favor, verifica que la placa sea correcta o consulte más tarde.");
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
                "✅ *%s*\n\n📋 *¿Qué deseas consultar?*\n\n- 1️⃣ Consultar una placa específica\n- 2️⃣ Consultar todas las placas programadas",
                dealershipName);
        messagePort.sendTextMessage(from, menu);
    }

    private void sendPlateDetails(String from, List<ServiceDelivery> services) {
        for (ServiceDelivery s : services) {
            // Enviar detalle de texto
            messagePort.sendTextMessage(from, formatServiceDetail(s));

            // Enviar ubicación nativa si existe GPS para el estado actual
            if (s.getHistory() != null && !s.getHistory().isEmpty()) {
                Optional<StatusHistory> lastUpdate = s.getHistory().stream()
                        .filter(h -> h.getNewStatus() == s.getCurrentStatus())
                        .max((h1, h2) -> h1.getChangeDate().compareTo(h2.getChangeDate()));

                if (lastUpdate.isPresent() && lastUpdate.get().getDeliveryLatitude() != null
                        && lastUpdate.get().getDeliveryLongitude() != null) {

                    double lat = lastUpdate.get().getDeliveryLatitude();
                    double lon = lastUpdate.get().getDeliveryLongitude();
                    String address = locationPort.reverseGeocode(new Location(lat, lon));

                    messagePort.sendLocation(from,
                            lat,
                            lon,
                            "Dirección: ",
                            address != null ? address : s.getPlate().getPlateNumber());
                }
            }
        }
    }

    private String formatServiceDetail(ServiceDelivery s) {
        String statusEmoji = getStatusEmoji(s.getCurrentStatus());
        String statusName = getStatusName(s.getCurrentStatus());

        return String.format(
                "🚗 *Placa: %s*\n\n*Estado:* %s %s\n📅 *Fecha de asignación:* %s\n🛵 *Mensajero:* %s\n🛞 *Concesionario:* %s",
                s.getPlate().getPlateNumber(),
                statusEmoji,
                statusName,
                s.getCreatedAt() != null ? s.getCreatedAt().format(DATE_FORMAT) : "No disponible",
                s.getMessenger() != null ? s.getMessenger().getFullName() : "Sin asignar",
                s.getDealership().getName());
    }

    private void sendPendingList(String from, Long dealershipId, String dealershipName) {
        List<ServiceDelivery> pending = searchService.findPendingByDealership(dealershipId);

        if (pending.isEmpty()) {
            messagePort.sendTextMessage(from, "✅ Todavia no hay placa(s) programada(s) para " + dealershipName);
            return;
        }

        StringBuilder sb = new StringBuilder("📦 *Placas programada(s) para " + dealershipName + "*\n\n");
        for (ServiceDelivery s : pending) {
            sb.append(formatServiceDetail(s));
        }
        messagePort.sendTextMessage(from, sb.toString().trim());
    }

    private String getStatusName(Status status) {
        return switch (status) {
            case PENDING -> "PENDIENTE";
            case ASSIGNED -> "ASIGNADO";
            case DELIVERED -> "ENTREGADO";
            case RETURNED -> "DEVUELTO";
            case CANCELED -> "CANCELADO";
            case RESOLVED -> "REVISADO";
            case FAILED -> "FALLIDO";
            case DELETED -> "ELIMINADO";
        };
    }

    private String getStatusEmoji(Status status) {
        return switch (status) {
            case PENDING -> "📝";
            case ASSIGNED -> "⏳";
            case DELIVERED -> "✅";
            case RETURNED -> "↩️";
            case CANCELED -> "❌";
            case RESOLVED -> "✍🏻";
            case FAILED -> "⚠️";
            case DELETED -> "🗑️";
        };
    }

    private boolean looksLikePlate(String text) {
        return text.matches("(?i)^[A-Z]{3}-?\\d{3}$") ||
                text.matches("(?i)^\\d{3}-?[A-Z]{3}$") ||
                text.matches("(?i)^[A-Z]{3}-?\\d{2}[A-Z]$");
    }

    private void scheduleTimeout(String from) {
        ScheduledFuture<?> future = scheduler.schedule(() -> {
            messagePort.sendTextMessage(from, "Han pasado 5 minutos desde tú ultimo mensaje, Avisame cuando quieras volver a consultar.");
            timeoutNotified.add(from);
            scheduledTimeouts.remove(from);
        }, 5, TimeUnit.MINUTES);
        scheduledTimeouts.put(from, future);
    }

    private void cancelTimeout(String from) {
        ScheduledFuture<?> future = scheduledTimeouts.remove(from);
        if (future != null) {
            future.cancel(false);
        }
    }

    @PreDestroy
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
