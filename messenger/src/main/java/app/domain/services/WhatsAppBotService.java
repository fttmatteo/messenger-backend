package app.domain.services;

import app.domain.model.Dealership;
import app.domain.model.ServiceDelivery;
import app.domain.model.StatusHistory;
import app.domain.model.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(WhatsAppBotService.class);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final Map<String, ScheduledFuture<?>> scheduledTimeouts = new ConcurrentHashMap<>();
    private final Set<String> timeoutNotified = ConcurrentHashMap.newKeySet();
    private final WhatsAppMessagePort messagePort;
    private final WhatsAppSessionPort sessionPort;
    private final SearchServiceDelivery searchService;
    private final LocationPort locationPort;
    private final Map<String, ConversationState> conversationStates = new ConcurrentHashMap<>();
    private final Map<String, Integer> failedAttempts = new ConcurrentHashMap<>();

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
        ConversationState currentState = conversationStates.getOrDefault(from, ConversationState.AWAITING_PIN);

        // Logging seguro: enmascarar teléfono y ocultar PIN si aplica
        String maskedFrom = maskPhone(from);
        String logContent = (currentState == ConversationState.AWAITING_PIN) ? "****" : text;
        logger.info("[WhatsApp] Mensaje recibido de {}: {}", maskedFrom, logContent);

        // Cancelar cualquier timeout previo
        cancelTimeout(from);
        // Verificar si venimos de un estado de inactividad
        boolean wasTimedOut = timeoutNotified.remove(from);
        // Verificar sesión
        Optional<WhatsAppSession> sessionOpt = sessionPort.findActiveSession(from);

        if (wasTimedOut) {
            if (sessionOpt.isPresent()) {
                messagePort.sendTextMessage(from, "¡Hola de nuevo! 👋");
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
            // 1. Verificar si está bloqueado por demasiados intentos
            int attempts = failedAttempts.getOrDefault(from, 0);
            if (attempts >= 3) {
                logger.warn("Usuario {} bloqueado por exceso de intentos de PIN.", from);
                messagePort.sendTextMessage(from,
                        "⚠️ Has superado el límite de intentos. Por seguridad, tu acceso ha sido pausado por 15 minutos.");
                return;
            }

            // Intentar autenticar con el PIN
            Optional<Dealership> dealershipOpt = sessionPort.findDealershipByPin(text);

            if (dealershipOpt.isPresent()) {
                // Éxito: Resetear intentos y crear sesión
                failedAttempts.remove(from);
                Dealership dealership = dealershipOpt.get();
                logger.info("[Sesión] Login exitoso para {} en concesionario: {}", maskPhone(from),
                        dealership.getName());
                sessionPort.createSession(from, dealership, sessionPort.getSessionExpirationHours());
                conversationStates.put(from, ConversationState.MENU);
                sendMenu(from, dealership.getName());
            } else {
                // Fallo: Incrementar intentos y aplicar delay progresivo
                attempts++;
                failedAttempts.put(from, attempts);

                // Aplicar delay progresivo (ej. 2s por cada fallo)
                try {
                    Thread.sleep(attempts * 2000L);
                } catch (InterruptedException ignored) {
                }

                if (attempts >= 3) {
                    messagePort.sendTextMessage(from,
                            "❌ PIN incorrecto. Has alcanzado el máximo de intentos permitidos. Por seguridad, tu acceso se ha bloqueado por 15 minutos.");

                    // Programar desbloqueo automático en 15 minutos
                    scheduler.schedule(() -> {
                        failedAttempts.remove(from);
                        logger.info("Bloqueo de PIN expirado para {}", from);
                    }, 15, TimeUnit.MINUTES);
                } else {
                    messagePort.sendTextMessage(from,
                            "❌ PIN incorrecto. Intenta de nuevo (Intento " + attempts + " de 3):");
                }
            }
        } else {
            conversationStates.put(from, ConversationState.AWAITING_PIN);
            messagePort.sendTextMessage(from,
                    "🚦 *Tránsito de Sabaneta*\n*Matrículas Iniciales* 🚦\n_Área de mensajería_\n\n¡Hola! 👋. Aquí podrás consultar el estado de las placas programadas para entrega.\n\n"
                            + "`Por seguridad, el PIN se solicita cada 12 horas o cuando se cierre y se vuelva a abrir la sesión.`\n\n"
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
                    case "0" -> {
                        logger.info("[Sesión] Usuario {} cerró sesión voluntariamente.", maskPhone(from));
                        sessionPort.deleteByPhoneNumber(from);
                        conversationStates.remove(from);
                        cancelTimeout(from);
                        messagePort.sendTextMessage(from,
                                "✅ Sesión cerrada correctamente.\n\nPara ingresar de nuevo, solo escribe un mensaje.");
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
                "🛞 *%s*\n\n📋 *¿Qué deseas consultar?*\n_Ingrese el número correspondiente:_\n\n- 1️⃣ Consultar una placa específica\n- 2️⃣ Consultar todas las placas programadas\n- 0️⃣ Cerrar sesión y salir",
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
                            "Ubicación del *estado* actual",
                            address != null ? address : s.getPlate().getPlateNumber());
                }
            }
        }
    }

    private String formatServiceDetail(ServiceDelivery s) {
        String statusEmoji = getStatusEmoji(s.getCurrentStatus());
        String statusName = getStatusName(s.getCurrentStatus());

        return String.format(
                "🚗 Placa: %s\n\n✏️ *Estado:* %s %s\n📅 *Fecha de asignación:* %s\n🛵 *Mensajero:* %s\n🛞 *Concesionario:* %s",
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
            messagePort.sendTextMessage(from,
                    "Todavia no hay placa(s) programada(s) para " + dealershipName + "\n\nConsulte más tarde.");
            return;
        }

        // Enviamos el encabezado primero
        messagePort.sendTextMessage(from, "📦 *Placa(s) programada(s) para " + dealershipName + "*");

        // Delegamos a sendPlateDetails para que cada placa muestre su detalle y
        // ubicación individualmente
        sendPlateDetails(from, pending);
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

    private String maskPhone(String phone) {
        if (phone == null || phone.length() <= 4) {
            return phone;
        }
        return "****" + phone.substring(phone.length() - 4);
    }

    private void scheduleTimeout(String from) {
        ScheduledFuture<?> future = scheduler.schedule(() -> {
            logger.info("[Sesión] Chat finalizado por inactividad para {}", maskPhone(from));
            messagePort.sendTextMessage(from,
                    "⏰ Por inactividad, hemos finalizado el chat. Si necesitas realizar una nueva consulta, ¡escríbeme! 👋");
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
