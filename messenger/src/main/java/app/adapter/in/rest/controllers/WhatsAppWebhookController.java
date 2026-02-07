package app.adapter.in.rest.controllers;

import app.adapter.in.rest.request.WebhookPayload;
import app.domain.services.WhatsAppBotService;
import app.infrastructure.config.WhatsAppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador para webhooks de WhatsApp Cloud API.
 */
@RestController
@RequestMapping("/api/whatsapp")
public class WhatsAppWebhookController {

    private static final Logger logger = LoggerFactory.getLogger(WhatsAppWebhookController.class);

    private final WhatsAppBotService botService;
    private final WhatsAppConfig config;

    public WhatsAppWebhookController(WhatsAppBotService botService, WhatsAppConfig config) {
        this.botService = botService;
        this.config = config;
    }

    /**
     * Verificación del webhook (requerido por Meta).
     */
    @GetMapping("/webhook")
    public ResponseEntity<String> verifyWebhook(
            @RequestParam("hub.mode") String mode,
            @RequestParam("hub.verify_token") String token,
            @RequestParam("hub.challenge") String challenge) {

        logger.info("Verificación de webhook: mode={}, token={}", mode, token);

        if ("subscribe".equals(mode) && config.getVerifyToken().equals(token)) {
            logger.info("Webhook verificado exitosamente");
            return ResponseEntity.ok(challenge);
        }

        logger.warn("Verificación fallida: token incorrecto");
        return ResponseEntity.status(403).body("Forbidden");
    }

    /**
     * Recepción de mensajes entrantes.
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> receiveMessage(@RequestBody WebhookPayload payload) {
        try {
            if (payload.getEntry() != null) {
                for (WebhookPayload.Entry entry : payload.getEntry()) {
                    if (entry.getChanges() != null) {
                        for (WebhookPayload.Change change : entry.getChanges()) {
                            if (change.getValue() != null && change.getValue().getMessages() != null) {
                                for (WebhookPayload.Message message : change.getValue().getMessages()) {
                                    if ("text".equals(message.getType()) && message.getText() != null) {
                                        String from = message.getFrom();
                                        String text = message.getText().getBody();
                                        botService.processMessage(from, text);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error procesando webhook: {}", e.getMessage(), e);
        }

        // Siempre responder 200 para evitar reintentos
        return ResponseEntity.ok("EVENT_RECEIVED");
    }
}
