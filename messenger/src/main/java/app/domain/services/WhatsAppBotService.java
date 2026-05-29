package app.domain.services;

import app.domain.model.Dealership;
import app.domain.model.ServiceDelivery;
import app.domain.util.LogSanitizer;
import app.domain.ports.ServiceDeliveryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import app.domain.model.WhatsAppSession;
import app.domain.model.enums.Status;
import app.domain.ports.WhatsAppMessagePort;
import app.domain.ports.WhatsAppSessionPort;
import app.domain.ports.WhatsAppRateLimitPort;
import app.domain.ports.StoragePort;
import app.domain.ports.WhatsAppUserTermsPort;
import app.infrastructure.config.WhatsAppConfig;
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
import app.domain.model.Photo;
import app.domain.model.StatusHistory;

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
    private final ServiceDeliveryPort searchService;
    private final WhatsAppRateLimitPort rateLimitPort;
    private final StoragePort storagePort;
    private final WhatsAppUserTermsPort userTermsPort;
    private final WhatsAppConfig config;

    public WhatsAppBotService(
            WhatsAppMessagePort messagePort,
            WhatsAppSessionPort sessionPort,
            ServiceDeliveryPort searchService,
            StoragePort storagePort,
            WhatsAppRateLimitPort rateLimitPort,
            WhatsAppUserTermsPort userTermsPort,
            WhatsAppConfig config) {
        this.messagePort = messagePort;
        this.sessionPort = sessionPort;
        this.searchService = searchService;
        this.rateLimitPort = rateLimitPort;
        this.storagePort = storagePort;
        this.userTermsPort = userTermsPort;
        this.config = config;
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
        String logContent = currentState == WhatsAppConversationState.AWAITING_PIN && sessionOpt.isEmpty() ? "****"
                : LogSanitizer.maskGeneric(text, 2);
        logger.debug("[WhatsApp] Mensaje recibido de {}: {}", maskedFrom, logContent);

        if (sessionOpt.isPresent()) {
            WhatsAppSession session = sessionOpt.get();
            if (session.isTimeoutNotified()) {
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
        if (!userTermsPort.hasAccepted(from)) {
            if ("ACCEPT_TERMS".equals(text) || "Acepto".equalsIgnoreCase(text.trim())) {
                userTermsPort.saveAcceptance(from);
                messagePort.sendTextMessage(from,
                        "¡Gracias por aceptar! 👋🏼.\nAquí podrás consultar el estado de las motos por chasis.\n\n"
                                + "_PIN requerido cada 12h o al reiniciar sesión._\n\n"
                                + "🔒 *Ingresa el PIN para continuar:* ");
            } else {
                String termsMsg = "¡Hola! 👋🏼 Bienvenido a PLAK.\n\n"
                        + "Antes de continuar, es necesario que leas y aceptes nuestros Términos y Condiciones y Política de Privacidad:\n\n"
                        + "📄 Términos y Condiciones: " + config.getFrontendUrl() + "/terminos-condiciones\n"
                        + "🔒 Política de Privacidad: " + config.getFrontendUrl() + "/politica-privacidad\n\n"
                        + "Por favor, presiona el botón 'Acepto' para confirmar.";
                messagePort.sendReplyButtons(from, termsMsg, java.util.List.of("Acepto"), java.util.List.of("ACCEPT_TERMS"));
            }
            return;
        }

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
                dealership.setName("Llave Maestra");
                WhatsAppSession session = sessionPort.createSession(from, dealership,
                        sessionPort.getSessionExpirationHours());
                session.setConversationState(WhatsAppConversationState.MENU);
                sessionPort.updateSession(session);
                logger.info("[Autenticación] Sesión llave maestra iniciada por el número {}", LogSanitizer.maskGeneric(from, 4));
                messagePort.sendTextMessage(from, "🔔 Notificaciones activadas. Recibirás alertas por cambios en el estado de las motos.\n\n_Si deseas dejar de recibirlas, simplemente usa la opción de Cerrar Sesión en el menú._");
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
                    logger.info("[Autenticación] Sesión iniciada por el número {}.", LogSanitizer.maskGeneric(from, 4));
                    messagePort.sendTextMessage(from, "🔔 Notificaciones activadas. Recibirás alertas por cambios en el estado de las motos.\n\n_Si deseas dejar de recibirlas, simplemente usa la opción de Cerrar Sesión en el menú._");
                    sendMenu(from, dealership.getName());
                } else {
                    int remaining = rateLimitPort.recordFailedAttempt(from);
                    int attemptsDone = 3 - remaining;

                    if (remaining == 0) {
                        logger.warn("[Autenticación] Intento fallido de PIN ({}/3) para {}. Cuenta bloqueada por 15 minutos.", attemptsDone, LogSanitizer.maskGeneric(from, 4));
                        messagePort.sendTextMessage(from,
                                "❌ PIN incorrecto. Has alcanzado el máximo de intentos permitidos. Por seguridad, tu acceso se ha bloqueado por 15 minutos.");
                    } else {
                        logger.warn("[Autenticación] Intento fallido de PIN ({}/3) para {}", attemptsDone, LogSanitizer.maskGeneric(from, 4));
                        messagePort.sendTextMessage(from,
                                "❌ PIN incorrecto. Intenta de nuevo (Intento " + attemptsDone + " de 3):");
                    }
                }
            }
        } else {
            messagePort.sendTextMessage(from,
                    "¡Hola! 👋🏼. Aquí podrás consultar el estado de las motos por chasis.\n\n"
                            + "_PIN requerido cada 12h o al reiniciar sesión._\n\n"
                            + "🔒 *Ingresa el PIN para continuar:* ");
        }
    }

    private void handleAuthenticatedUser(String from, String text, WhatsAppSession session) {
        Long dealershipId = session.getDealership().getIdDealership();
        String dealershipName = session.getDealership().getName();

        if (text.startsWith("VIEW_PHOTOS_")) {
            try {
                Long serviceId = Long.parseLong(text.substring("VIEW_PHOTOS_".length()));
                ServiceDelivery s = searchService.findByIdActive(serviceId);
                if (s == null) throw new RuntimeException("Service not found");
                Optional<StatusHistory> currentHistoryOpt = s.getHistory().stream()
                        .filter(h -> h.getNewStatus() == s.getCurrentStatus())
                        .reduce((first, second) -> second);
                
                if (currentHistoryOpt.isPresent() && currentHistoryOpt.get().getPhotos() != null && !currentHistoryOpt.get().getPhotos().isEmpty()) {
                    List<Photo> photos = currentHistoryOpt.get().getPhotos();
                    String plateNumber = s.getPlate() != null && s.getPlate().getPlateNumber() != null 
                            ? s.getPlate().getPlateNumber() 
                            : "Desconocido";
                    if (photos.size() == 1) {
                        Photo p = photos.get(0);
                        String url = storagePort.getUrl(p.getPhotoPath());
                        String caption = "📸 Foto de evidencia";
                        String fileName = String.format("Foto_%s.webp", plateNumber);
                        messagePort.sendDocument(from, url, caption, fileName);
                    } else {
                        java.io.File tempZip = java.nio.file.Files.createTempFile("fotos_" + plateNumber, ".zip").toFile();
                        try (java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(new java.io.FileOutputStream(tempZip))) {
                            for (int i = 0; i < photos.size(); i++) {
                                Photo p = photos.get(i);
                                try (java.io.InputStream photoStream = storagePort.get(p.getPhotoPath())) {
                                    if (photoStream != null) {
                                        java.util.zip.ZipEntry zipEntry = new java.util.zip.ZipEntry(String.format("%d_Foto_%s.webp", i + 1, plateNumber));
                                        zos.putNextEntry(zipEntry);
                                        photoStream.transferTo(zos);
                                        zos.closeEntry();
                                    }
                                }
                            }
                        }
                        
                        String zipFileName = String.format("Fotos_%s.zip", plateNumber);
                        String savedZipPath = storagePort.save(tempZip, "zips", zipFileName);
                        String zipUrl = storagePort.getUrl(savedZipPath);
                        
                        String caption = String.format("📸 %d Fotos de evidencia (Comprimidas)", photos.size());
                        messagePort.sendDocument(from, zipUrl, caption, zipFileName);
                        
                        tempZip.delete();
                    }
                    sleep(1500);
                } else {
                    messagePort.sendTextMessage(from, "⚠️ No se encontraron fotos para el estado actual de este chasis.");
                }
            } catch (Exception e) {
                logger.error("[WhatsApp] Error al obtener fotos para servicio", e);
                messagePort.sendTextMessage(from, "⚠️ Hubo un error al intentar obtener las fotos.");
            }
            sendMenu(from, dealershipName);
            return;
        }

        if (text.startsWith("VIEW_PLATE_")) {
            String plate = text.substring("VIEW_PLATE_".length());
            Page<ServiceDelivery> servicesPage = searchService.findByPlateAndDealershipPaginated(plate.trim().toUpperCase(), dealershipId,
                    PageRequest.of(0, 5));
            if (!servicesPage.isEmpty()) {
                sendPlateDetails(from, servicesPage.getContent());
            } else {
                messagePort.sendTextMessage(from,
                        "⚠️ No se encontró el chasis *" + plate.toUpperCase() + "* en " + dealershipName + ".\n\n"
                                + "Por favor, verifica que el número sea correcto o consulta más tarde.");
                sendMenu(from, dealershipName);
            }
            session.setConversationState(WhatsAppConversationState.MENU);
            sessionPort.updateSession(session);
            return;
        }

        WhatsAppConversationState state = session.getConversationState();

        switch (state) {
            case AWAITING_PLATE -> {
                Page<ServiceDelivery> servicesPage = searchService.findByPlateAndDealershipPaginated(text.trim().toUpperCase(), dealershipId,
                        PageRequest.of(0, 5));
                if (servicesPage.isEmpty()) {
                    messagePort.sendTextMessage(from,
                            "⚠️ No se encontró el chasis *" + text.toUpperCase() + "* en " + dealershipName + ".\n\n"
                                    + "Por favor, verifica que el número sea correcto o consulta más tarde.");
                    sendMenu(from, dealershipName);
                } else {
                    sendPlateDetails(from, servicesPage.getContent());
                }
                session.setConversationState(WhatsAppConversationState.MENU);
                sessionPort.updateSession(session);
            }
            case MENU -> {
                if (text.startsWith("NEXT_PAGE") || text.startsWith("PREV_PAGE")) {
                    int newPage = text.startsWith("NEXT_PAGE") 
                            ? session.getCurrentPage() + 1 
                            : Math.max(0, session.getCurrentPage() - 1);
                    session.setCurrentPage(newPage);
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
                                "🚪 Sesión cerrada correctamente.\n\n🔕 Notificaciones desactivadas. A partir de este momento ya no recibirás alertas de cambio de estado.\n\n¡Hasta pronto! 👋. Para ingresar de nuevo, solo escribe un mensaje.");
                    }
                    default -> {
                        if (looksLikePlate(text)) {
                            Page<ServiceDelivery> servicesPage = searchService.findByPlateAndDealershipPaginated(text.trim().toUpperCase(),
                                    dealershipId, PageRequest.of(0, 5));
                            if (!servicesPage.isEmpty()) {
                                sendPlateDetails(from, servicesPage.getContent());
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
        String listTitle = "Menú principal";

        List<String> rowTitles = List.of(
                "🔍 Consulta específica",
                "⏳ Chasis asignados",
                "↩️ Chasis devueltos",
                "📝 Chasis pendientes",
                "✅ Chasis entregados",
                "🚪 Cerrar sesión");

        List<String> rowIds = List.of("1", "2", "3", "4", "5", "0");

        messagePort.sendListMessage(from, bodyText, buttonText, listTitle, rowTitles, null, rowIds);
    }

    private void sendPlateDetails(String from, List<ServiceDelivery> services) {
        for (ServiceDelivery s : services) {
            String message = formatServiceDetail(s);
            
            boolean hasPhotos = false;
            if (s.getHistory() != null && !s.getHistory().isEmpty()) {
                Optional<StatusHistory> currentHistoryOpt = s.getHistory().stream()
                        .filter(h -> h.getNewStatus() == s.getCurrentStatus())
                        .reduce((first, second) -> second);
                if (currentHistoryOpt.isPresent() && currentHistoryOpt.get().getPhotos() != null && !currentHistoryOpt.get().getPhotos().isEmpty()) {
                    hasPhotos = true;
                }
            }

            if (hasPhotos) {
                messagePort.sendReplyButtons(from, message, List.of("Ver fotos", "Menú principal"), List.of("VIEW_PHOTOS_" + s.getIdServiceDelivery(), "MENU_BACK"));
            } else {
                messagePort.sendReplyButtons(from, message, List.of("Menú principal"), List.of("MENU_BACK"));
            }
            sleep(500);
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
        String plateNumber = s.getPlate() != null && s.getPlate().getPlateNumber() != null 
                ? s.getPlate().getPlateNumber() 
                : "Desconocido";

        String statusEmoji = (s.getCurrentStatus() != null) ? getStatusEmoji(s.getCurrentStatus()) : "❓";
        String statusName = (s.getCurrentStatus() != null) ? getStatusName(s.getCurrentStatus()) : "DESCONOCIDO";

        String originDealershipName = s.getOriginDealership() != null
                ? s.getOriginDealership().getName()
                : "No disponible";
                
        String destinationName = s.getDealership() != null && s.getDealership().getName() != null
                ? s.getDealership().getName()
                : "No disponible";

        StringBuilder message = new StringBuilder();
        message.append(String.format("🛵 *%s*\n\n", plateNumber));
        message.append(String.format("*ESTADO:* %s %s\n", statusEmoji, statusName));
        if (s.getCurrentStatus() == Status.SCHEDULED) {
            message.append(String.format("⏱️ *Fecha programada:* %s\n", s.getScheduledAt() != null ? s.getScheduledAt().format(DATE_FORMAT) : "No disponible"));
        } else {
            message.append(String.format("⏱️ *Fecha de asignación:* %s\n", s.getCreatedAt() != null ? s.getCreatedAt().format(DATE_FORMAT) : "No disponible"));
        }

        if (s.getCurrentStatus() == Status.DELIVERED || s.getCurrentStatus() == Status.RESOLVED) {
            String deliveryDateStr = "No disponible";
            if (s.getHistory() != null) {
                deliveryDateStr = s.getHistory().stream()
                        .filter(h -> h.getNewStatus() == Status.DELIVERED)
                        .map(h -> h.getChangeDate().format(DATE_FORMAT))
                        .findFirst()
                        .orElse("No disponible");
            }
            message.append(String.format("📦 *Fecha de entrega:* %s\n", deliveryDateStr));
        }

        message.append(String.format("🚚 *Transportista:* %s\n", s.getMessenger() != null ? s.getMessenger().getFullName() : "Sin asignar"));
        message.append(String.format("📍 *Origen:* %s\n", originDealershipName));
        message.append(String.format("🏁 *Destino:* %s", destinationName));

        String currentObservation = null;
        if (s.getHistory() != null && !s.getHistory().isEmpty()) {
            currentObservation = s.getHistory().stream()
                    .filter(h -> h.getNewStatus() == s.getCurrentStatus())
                    .reduce((first, second) -> second)
                    .map(h -> h.getObservation())
                    .orElse(null);
        }

        if (currentObservation != null && !currentObservation.trim().isEmpty()) {
            message.append(String.format("\n💬 *Observación:* %s", currentObservation.trim()));
        }

        return message.toString();
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
        Page<ServiceDelivery> resultPage = searchService.findByDealershipIdAndStatusesPaginated(
                dealershipId, statuses, PageRequest.of(page, 10, Sort.by("createdAt").descending()));

        if (resultPage.isEmpty() && page == 0) {
            messagePort.sendTextMessage(from,
                    "Todavía no hay " + title.toLowerCase() + " para *" + dealershipName + "*.\n\nConsulte más tarde.");
            sendMenu(from, dealershipName);
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("*").append(title).append("*\n");
        sb.append("_Página ").append(page + 1).append(" de ").append(resultPage.getTotalPages()).append("_\n\n");

        for (ServiceDelivery s : resultPage.getContent()) {
            sb.append("• ").append(s.getPlate().getPlateNumber()).append("\n");
        }

        sb.append("\n_Escribe el número del chasis para ver detalles._");

        boolean hasNext = resultPage.hasNext();
        boolean hasPrevious = page > 0;

        List<String> buttonNames = new java.util.ArrayList<>();
        List<String> buttonIds = new java.util.ArrayList<>();

        if (hasPrevious) {
            buttonNames.add("Atrás");
            buttonIds.add("PREV_PAGE");
        }
        if (hasNext) {
            buttonNames.add("Ver más");
            buttonIds.add("NEXT_PAGE");
        }
        
        buttonNames.add("Menú principal");
        buttonIds.add("MENU_BACK");

        messagePort.sendReplyButtons(from, sb.toString(), buttonNames, buttonIds);
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
            case ASSIGNED -> "ASIGNADA";
            case DELIVERED -> "ENTREGADA";
            case RETURNED -> "DEVUELTA";
            case CANCELED -> "CANCELADA";
            case RESOLVED -> "REVISADA";
            case FAILED -> "FALLIDA";
            case DELETED -> "ELIMINADA";
            case SCHEDULED -> "PROGRAMADA";
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
            case SCHEDULED -> "📅";
        };
    }

    private boolean looksLikePlate(String text) {
        return text.trim().matches("^[A-Z0-9]{10,20}$");
    }

    /**
     * Maneja tipos de mensajes no soportados (stickers, imágenes, audios, etc.)
     */
    @Transactional
    public void handleUnsupportedMessageType(String from, String type) {
        Optional<WhatsAppSession> sessionOpt = sessionPort.findActiveSession(from);
        String maskedFrom = LogSanitizer.maskGeneric(from, 4);
        logger.warn("[WhatsApp] Tipo de mensaje no soportado '{}' recibido de {}", type, maskedFrom);

        String translatedType = translateMessageType(type);

        if (sessionOpt.isPresent()) {
            WhatsAppSession session = sessionOpt.get();
            if (session.isTimeoutNotified()) {
                session.setTimeoutNotified(false);
            }
            session.setLastActivityAt(java.time.LocalDateTime.now());
            sessionPort.updateSession(session);

            messagePort.sendTextMessage(from, "⚠️ Lo siento, no puedo procesar este tipo de mensaje (" + translatedType + ").\n\nPor favor, utiliza las opciones del menú o envía texto válido.");
            sendMenu(from, session.getDealership().getName());
        } else {
            messagePort.sendTextMessage(from, "⚠️ Lo siento, no puedo procesar este tipo de mensaje (" + translatedType + ").\n\nPor favor, ingresa tu PIN para continuar.");
        }
    }

    private String translateMessageType(String type) {
        if (type == null) return "archivo multimedia";
        return switch (type) {
            case "sticker" -> "sticker";
            case "image" -> "imagen";
            case "audio" -> "audio";
            case "video" -> "video";
            case "document" -> "documento";
            case "location" -> "ubicación";
            case "contacts" -> "contactos";
            default -> "archivo multimedia";
        };
    }
}
