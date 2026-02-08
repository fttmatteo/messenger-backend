package app.infrastructure.external;

import app.domain.ports.WhatsAppMessagePort;
import app.infrastructure.config.WhatsAppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
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
