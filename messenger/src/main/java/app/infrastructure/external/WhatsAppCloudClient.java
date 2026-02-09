package app.infrastructure.external;

import app.domain.ports.WhatsAppMessagePort;
import app.infrastructure.config.WhatsAppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Cliente HTTP para comunicación con WhatsApp Cloud API.
 * Implementa el puerto del dominio.
 */
@Component
public class WhatsAppCloudClient implements WhatsAppMessagePort {

    private static final Logger logger = LoggerFactory.getLogger(WhatsAppCloudClient.class);

    private final WhatsAppConfig config;
    private final RestTemplate restTemplate;

    public WhatsAppCloudClient(WhatsAppConfig config) {
        this.config = config;
        this.restTemplate = new RestTemplate();
    }

    /**
     * Envía un mensaje de texto a un número de WhatsApp.
     */
    @Override
    public boolean sendTextMessage(String to, String message) {
        return sendMessage(to, "text", Map.of("body", message));
    }

    /**
     * Envía una ubicación geográfica nativa a un número de WhatsApp.
     */
    @Override
    public boolean sendLocation(String to, double latitude, double longitude, String name, String address) {
        Map<String, Object> location = new HashMap<>();
        location.put("latitude", latitude);
        location.put("longitude", longitude);
        location.put("name", name);
        location.put("address", address);
        return sendMessage(to, "location", location);
    }

    @Override
    public boolean sendReplyButtons(String to, String bodyText, List<String> buttonTitles, List<String> buttonIds) {
        if (buttonTitles == null || buttonIds == null || buttonTitles.size() != buttonIds.size()) {
            logger.error("[WhatsApp] Error en parámetros de botones: títulos e IDs no coinciden.");
            return false;
        }

        List<Map<String, Object>> buttons = new ArrayList<>();
        for (int i = 0; i < buttonTitles.size(); i++) {
            Map<String, Object> button = Map.of(
                    "type", "reply",
                    "reply", Map.of(
                            "id", buttonIds.get(i),
                            "title", buttonTitles.get(i)));
            buttons.add(button);
        }

        Map<String, Object> interactive = Map.of(
                "type", "button",
                "body", Map.of("text", bodyText),
                "action", Map.of("buttons", buttons));

        return sendMessage(to, "interactive", interactive);
    }

    @Override
    public boolean sendListMessage(String to, String bodyText, String buttonText, String listTitle,
            List<String> rowTitles, List<String> rowDescriptions, List<String> rowIds) {
        if (rowTitles == null || rowIds == null || rowTitles.size() != rowIds.size()) {
            logger.error("[WhatsApp] Error en parámetros de lista: títulos e IDs no coinciden.");
            return false;
        }

        List<Map<String, Object>> rows = new ArrayList<>();
        for (int i = 0; i < rowTitles.size(); i++) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", rowIds.get(i));
            row.put("title", rowTitles.get(i));
            if (rowDescriptions != null && i < rowDescriptions.size() && rowDescriptions.get(i) != null) {
                row.put("description", rowDescriptions.get(i));
            }
            rows.add(row);
        }

        Map<String, Object> section = Map.of(
                "title", listTitle,
                "rows", rows);

        Map<String, Object> interactive = Map.of(
                "type", "list",
                "body", Map.of("text", bodyText),
                "action", Map.of(
                        "button", buttonText,
                        "sections", List.of(section)));

        return sendMessage(to, "interactive", interactive);
    }

    private boolean sendMessage(String to, String type, Object data) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(config.getAccessToken());

            Map<String, Object> body = new HashMap<>();
            body.put("messaging_product", "whatsapp");
            body.put("to", to);
            body.put("type", type);
            body.put(type, data);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    config.getMessagesUrl(),
                    HttpMethod.POST,
                    request,
                    String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("[WhatsApp] Mensaje {} enviado a {}", type, maskPhone(to));
                return true;
            } else {
                logger.error("[WhatsApp] Error enviando mensaje {}: {}", type, response.getBody());
                return false;
            }
        } catch (Exception e) {
            logger.error("[WhatsApp] Error enviando mensaje {} a WhatsApp: {}", type, e.getMessage());
            return false;
        }
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() <= 4) {
            return phone;
        }
        return "****" + phone.substring(phone.length() - 4);
    }
}
