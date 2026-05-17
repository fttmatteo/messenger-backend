package app.domain.services;

import app.domain.model.Dealership;
import app.domain.model.ServiceDelivery;
import app.domain.model.StatusHistory;
import app.domain.model.Location;
import app.domain.util.LogSanitizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import app.domain.model.WhatsAppSession;
import app.domain.model.enums.Status;
import app.domain.ports.LocationPort;
import app.domain.ports.WhatsAppMessagePort;
import app.domain.ports.WhatsAppSessionPort;
import app.domain.ports.WhatsAppRateLimitPort;
import app.domain.ports.StoragePort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import app.domain.model.enums.WhatsAppConversationState;
import java.util.stream.Collectors;

/**
 * Servicio principal del bot de WhatsApp.
 * Maneja autenticación por PIN y consultas de chasis.
 * Usa ports para desacoplarse de la infraestructura (arquitectura hexagonal).
 */
@Service
public class WhatsAppBotService {

    private static final Logger logger = LoggerFactory.getLogger(WhatsAppBotService.class);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final WhatsAppMessagePort messagePort;
    private final WhatsAppSessionPort sessionPort;
    private final SearchServiceDelivery searchService;
    private final LocationPort locationPort;
    private final WhatsAppRateLimitPort rateLimitPort;

    public WhatsAppBotService(
            WhatsAppMessagePort messagePort,
            WhatsAppSessionPort sessionPort,
            SearchServiceDelivery searchService,
            LocationPort locationPort,
            StoragePort storagePort,
            WhatsAppRateLimitPort rateLimitPort) {
        this.messagePort = messagePort;
        this.sessionPort = sessionPort;
        this.searchService = searchService;
        this.locationPort = locationPort;
        this.rateLimitPort = rateLimitPort;
    }

    /**
     * Procesa un mensaje entrante de WhatsApp.
     */
    @Transactional
    public void processMessage(String from, String messageBody) {
        String text = messageBody.trim();

        Optional<WhatsAppSession> sessionOpt = sessionPort.findActiveSession(from);
        WhatsAppConversationState currentState = sessionOpt.map(WhatsAppSession::getConversationState)
                .orElse(WhatsAppConversationState.AWAITING_PIN);

        String maskedFrom = LogSanitizer.maskGeneric(from, 4);
        String logContent = (currentState == WhatsAppConversationState.AWAITING_PIN && sessionOpt.isEmpty()) ? "****"
                : LogSanitizer.maskGeneric(text, 2);
        logger.debug("[WhatsApp] Mensaje recibido de {}: {}", maskedFrom, logContent);

        if (sessionOpt.isPresent()) {
            WhatsAppSession session = sessionOpt.get();
            if (session.isTimeoutNotified()) {
                messagePort.sendTextMessage(from, "¡Hola de nuevo! 👋");
                session.setTimeoutNotified(false);
            }
            session.setLastActivityAt(java.time.LocalDateTime.now());
            sessionPort.updateSession(session);

            handleAuthenticatedUser(from, text, session);
        } else {
            handleUnauthenticatedUser(from, text);
        }
    }

    private void handleUnauthenticatedUser(String from, String text) {
        if (rateLimitPort.isBlocked(from)) {
            logger.warn("Usuario {} bloqueado por exceso de intentos de PIN.", LogSanitizer.maskGeneric(from, 4));
            messagePort.sendTextMessage(from,
                    "⚠️ Has superado el límite de intentos. Por seguridad, tu acceso ha sido pausado por 15 minutos.");
            return;
        }

        if (text.matches("\\d{4}")) {
            if (sessionPort.isMasterPin(text)) {
                rateLimitPort.clearFailedAttempts(from);
                Dealership dealership = new Dealership();
                dealership.setIdDealership(null);
                dealership.setName("PLAK Corporativo");
                WhatsAppSession session = sessionPort.createSession(from, dealership,
                        sessionPort.getSessionExpirationHours());
                session.setConversationState(WhatsAppConversationState.MENU);
                sessionPort.updateSession(session);
                sendMenu(from, dealership.getName());
            } else {
                Optional<Dealership> dealershipOpt = sessionPort.findDealershipByPin(text);

                if (dealershipOpt.isPresent()) {
                    rateLimitPort.clearFailedAttempts(from);
                    Dealership dealership = dealershipOpt.get();
                    WhatsAppSession session = sessionPort.createSession(from, dealership,
                            sessionPort.getSessionExpirationHours());
                    session.setConversationState(WhatsAppConversationState.MENU);
                    sessionPort.updateSession(session);
                    sendMenu(from, dealership.getName());
                } else {
                    int remaining = rateLimitPort.recordFailedAttempt(from);
                    int attemptsDone = 3 - remaining;

                    if (remaining == 0) {
                        messagePort.sendTextMessage(from,
                                "❌ PIN incorrecto. Has alcanzado el máximo de intentos permitidos. Por seguridad, tu acceso se ha bloqueado por 15 minutos.");
                    } else {
                        messagePort.sendTextMessage(from,
                                "❌ PIN incorrecto. Intenta de nuevo (Intento " + attemptsDone + " de 3):");
                    }
                }
            }
        } else {
            messagePort.sendTextMessage(from,
                    "🚦 *PLAK* 🚦\n¡Hola! 👋🏼. Aquí podrás consultar el estado de las motos por chasis.\n"
                            + "🔔 Mantén la sesión activa para recibir notificaciones de estados.\n"
                            + "`PIN requerido cada 12h o al reiniciar sesión.`\n\n"
                            + "🔒 *Ingresa el PIN para continuar:* ");
        }
    }

    private void handleAuthenticatedUser(String from, String text, WhatsAppSession session) {
        WhatsAppConversationState state = session.getConversationState();
        Long dealershipId = session.getDealership().getIdDealership();
        String dealershipName = session.getDealership().getName();

        switch (state) {
            case AWAITING_PLATE -> {
                Page<ServiceDelivery> servicesPage = searchService.findByPlateAndDealershipPaginated(text, dealershipId,
                        PageRequest.of(0, 5));
                if (servicesPage.isEmpty()) {
                    messagePort.sendTextMessage(from,
                            "⚠️ No se encontró el chasis *" + text.toUpperCase() + "* en " + dealershipName + ".\n\n"
                                    + "Por favor, verifica que el número sea correcto o consulta más tarde.");
                } else {
                    sendPlateDetails(from, servicesPage.getContent());
                }
                session.setConversationState(WhatsAppConversationState.MENU);
                sessionPort.updateSession(session);
                sendMenu(from, dealershipName);
            }
            case MENU -> {
                if (text.startsWith("NEXT_PAGE")) {
                    int nextPage = session.getCurrentPage() + 1;
                    session.setCurrentPage(nextPage);
                    sessionPort.updateSession(session);

                    List<Status> statuses = deserializeStatuses(session.getLastFilterStatuses());
                    String title = getTitleForStatuses(statuses);
                    sendFilteredList(from, session, title, statuses);
                    return;
                }

                if ("MENU_BACK".equals(text)) {
                    session.setCurrentPage(0);
                    session.setLastFilterStatuses("");
                    sessionPort.updateSession(session);
                    sendMenu(from, dealershipName);
                    return;
                }

                switch (text) {
                    case "1" -> {
                        session.setConversationState(WhatsAppConversationState.AWAITING_PLATE);
                        sessionPort.updateSession(session);
                        messagePort.sendTextMessage(from, "Escribe el número del chasis:");
                    }
                    case "2" -> {
                        sendFilteredList(from, session, "Chasis asignado(s)", List.of(Status.ASSIGNED));
                    }
                    case "3" -> {
                        sendFilteredList(from, session, "Chasis devuelto(s)", List.of(Status.RETURNED));
                    }
                    case "4" -> {
                        sendFilteredList(from, session, "Chasis pendiente(s)", List.of(Status.PENDING));
                    }
                    case "5" -> {
                        sendFilteredList(from, session, "Chasis entregado(s) y/o revisado(s)",
                                List.of(Status.DELIVERED, Status.RESOLVED));
                    }
                    case "0" -> {
                        logger.info("[Sesión] Usuario {} cerró sesión voluntariamente.",
                                LogSanitizer.maskGeneric(from, 4));
                        sessionPort.deleteByPhoneNumber(from);
                        messagePort.sendTextMessage(from,
                                "🚪 Sesión cerrada correctamente.\n\n¡Hasta pronto! 👋. Para ingresar de nuevo, solo escribe un mensaje.");
                    }
                    default -> {
                        if (looksLikePlate(text)) {
                            Page<ServiceDelivery> servicesPage = searchService.findByPlateAndDealershipPaginated(text,
                                    dealershipId, PageRequest.of(0, 5));
                            if (!servicesPage.isEmpty()) {
                                sendPlateDetails(from, servicesPage.getContent());
                                sendMenu(from, dealershipName);
                            } else {
                                messagePort.sendTextMessage(from,
                                        "⚠️ No se encontró el chasis *" + text.toUpperCase() + "* en " + dealershipName
                                                + ".\n\n"
                                                + "Por favor, verifica que el número sea correcto o consulta más tarde.");
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
        String bodyText = String.format("🛞 *%s*\n📋 *¿Qué deseas consultar?*", dealershipName);
        String buttonText = "Ver opciones";
        String listTitle = "Menú Principal";

        List<String> rowTitles = List.of(
                "🔍 Consulta específica",
                "⏳ Chasis asignados",
                "↩️ Chasis devueltos",
                "📝 Chasis pendientes",
                "✅ Chasis entregados",
                "🚪 Cerrar sesión");

        List<String> rowDescriptions = List.of(
                "Por chasis",
                "Para entrega",
                "Por intento fallido",
                "Por documentación",
                "Consolidado final",
                "Finalizar sesión actual");

        List<String> rowIds = List.of("1", "2", "3", "4", "5", "0");

        messagePort.sendListMessage(from, bodyText, buttonText, listTitle, rowTitles, rowDescriptions, rowIds);
    }

    private void sendPlateDetails(String from, List<ServiceDelivery> services) {
        for (ServiceDelivery s : services) {


            messagePort.sendTextMessage(from, formatServiceDetail(s));
            sleep(500);

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
                            "Ubicación del estado",
                            address != null ? address : s.getPlate().getPlateNumber());
                }
            }
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String formatServiceDetail(ServiceDelivery s) {
        String statusEmoji = getStatusEmoji(s.getCurrentStatus());
        String statusName = getStatusName(s.getCurrentStatus());

        return String.format(
                "🟡 *%s*\n\n✏️ *Estado:* %s %s\n📅 *Fecha de asignación:* %s\n🛵 *Mensajero:* %s\n🛞 *Concesionario:* %s",
                s.getPlate().getPlateNumber(),
                statusEmoji,
                statusName,
                s.getCreatedAt() != null ? s.getCreatedAt().format(DATE_FORMAT) : "No disponible",
                s.getMessenger() != null ? s.getMessenger().getFullName() : "Sin asignar",
                s.getDealership().getName());
    }

    private void sendFilteredList(String from, WhatsAppSession session, String title, List<Status> statuses) {
        Long dealershipId = session.getDealership().getIdDealership();
        String dealershipName = session.getDealership().getName();

        String serializedStatuses = serializeStatuses(statuses);
        if (!serializedStatuses.equals(session.getLastFilterStatuses())) {
            session.setCurrentPage(0);
            session.setLastFilterStatuses(serializedStatuses);
            sessionPort.updateSession(session);
        }

        int page = session.getCurrentPage();
        Page<ServiceDelivery> resultPage = searchService.findByDealershipAndStatusesPaginated(
                dealershipId, statuses, PageRequest.of(page, 10, Sort.by("createdAt").descending()));

        if (resultPage.isEmpty() && page == 0) {
            messagePort.sendTextMessage(from,
                    "Todavía no hay " + title.toLowerCase() + " para *" + dealershipName + "*.\n\nConsulte más tarde.");
            sendMenu(from, dealershipName);
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("📦 *").append(title).append("*\n");
        sb.append("_Página ").append(page + 1).append(" de ").append(resultPage.getTotalPages()).append("_\n\n");

        for (ServiceDelivery s : resultPage.getContent()) {
            sb.append("• ").append(s.getPlate().getPlateNumber())
                    .append(" (").append(getStatusName(s.getCurrentStatus())).append(")\n");
        }

        sb.append("\n_Escribe el número del chasis para ver detalles._");

        if (resultPage.hasNext()) {
            messagePort.sendReplyButtons(from, sb.toString(),
                    List.of("Ver más", "Menú Principal"),
                    List.of("NEXT_PAGE", "MENU_BACK"));
        } else {
            messagePort.sendReplyButtons(from, sb.toString(),
                    List.of("Menú Principal"),
                    List.of("MENU_BACK"));
        }
    }

    private String serializeStatuses(List<Status> statuses) {
        return statuses.stream().map(Enum::name).collect(Collectors.joining(","));
    }

    private List<Status> deserializeStatuses(String s) {
        if (s == null || s.isEmpty())
            return List.of();
        return Arrays.stream(s.split(","))
                .map(Status::valueOf)
                .collect(Collectors.toList());
    }

    private String getTitleForStatuses(List<Status> statuses) {
        if (statuses.contains(Status.ASSIGNED))
            return "Chasis asignado(s)";
        if (statuses.contains(Status.RETURNED))
            return "Chasis devuelto(s)";
        if (statuses.contains(Status.PENDING))
            return "Chasis pendiente(s)";
        if (statuses.contains(Status.DELIVERED))
            return "Chasis entregado(s) y/o revisado(s)";
        return "Listado de chasis";
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
        return text.trim().matches("^[A-Z0-9]{10,20}$");
    }
}
