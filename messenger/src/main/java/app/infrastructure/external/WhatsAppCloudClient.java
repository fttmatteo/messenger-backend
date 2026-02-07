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
    public boolean sendTextMessage(String to, String message) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(config.getAccessToken());

            Map<String, Object> body = new HashMap<>();
            body.put("messaging_product", "whatsapp");
            body.put("to", to);
            body.put("type", "text");

            Map<String, String> text = new HashMap<>();
            text.put("body", message);
            body.put("text", text);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    config.getMessagesUrl(),
                    HttpMethod.POST,
                    request,
                    String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("Mensaje enviado a {}", to);
                return true;
            } else {
                logger.error("Error enviando mensaje: {}", response.getBody());
                return false;
            }
        } catch (Exception e) {
            logger.error("Error enviando mensaje a WhatsApp: {}", e.getMessage());
            return false;
        }
    }
}
