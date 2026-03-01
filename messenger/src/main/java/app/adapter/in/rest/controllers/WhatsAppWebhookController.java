package app.adapter.in.rest.controllers;

import app.adapter.in.rest.request.WebhookPayload;
import app.domain.services.WhatsAppBotService;
import app.infrastructure.config.WhatsAppConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

/**
 * Controlador para webhooks de WhatsApp Cloud API.
 */
@RestController
@RequestMapping("/api/whatsapp")
public class WhatsAppWebhookController {

    private static final Logger logger = LoggerFactory.getLogger(WhatsAppWebhookController.class);

    private final WhatsAppBotService botService;
    private final WhatsAppConfig config;
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String DEDUP_PREFIX = "wa:msg:dedup:";
    private static final Duration DEDUP_TTL = Duration.ofMinutes(5);

    public WhatsAppWebhookController(WhatsAppBotService botService, WhatsAppConfig config,
            ObjectMapper objectMapper, RedisTemplate<String, String> redisTemplate) {
        this.botService = botService;
        this.config = config;
        this.objectMapper = objectMapper;
        this.redisTemplate = redisTemplate;
    }

    /**
     * Verificación del webhook (requerido por Meta).
     */
    @GetMapping("/webhook")
    public ResponseEntity<String> verifyWebhook(
            @RequestParam("hub.mode") String mode,
            @RequestParam("hub.verify_token") String token,
            @RequestParam("hub.challenge") String challenge) {

        if ("subscribe".equals(mode) && config.getVerifyToken().equals(token)) {
            return ResponseEntity.ok(challenge);
        }

        logger.warn("[Webhook] Verificación fallida: token incorrecto");
        return ResponseEntity.status(403).body("Forbidden");
    }

    /**
     * Recepción de mensajes entrantes.
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> receiveMessage(
            @RequestBody String rawBody,
            @RequestHeader(value = "X-Hub-Signature-256", required = false) String signature) {

        if (!isValidSignature(rawBody, signature)) {
            logger.error("Firma de webhook inválida. Petición rechazada.");
            return ResponseEntity.status(403).body("Invalid signature");
        }

        try {
            WebhookPayload payload = objectMapper.readValue(rawBody, WebhookPayload.class);
            if (payload.getEntry() != null) {
                for (WebhookPayload.Entry entry : payload.getEntry()) {
                    if (entry.getChanges() != null) {
                        for (WebhookPayload.Change change : entry.getChanges()) {
                            if (change.getValue() != null && change.getValue().getMessages() != null) {
                                for (WebhookPayload.Message message : change.getValue().getMessages()) {
                                    if (isDuplicate(message.getId())) {
                                        continue;
                                    }
                                    if ("text".equals(message.getType()) && message.getText() != null) {
                                        String from = message.getFrom();
                                        String text = message.getText().getBody();
                                        botService.processMessage(from, text);
                                    } else if ("interactive".equals(message.getType())
                                            && message.getInteractive() != null) {
                                        String from = message.getFrom();
                                        WebhookPayload.Interactive interactive = message.getInteractive();
                                        if ("button_reply".equals(interactive.getType())
                                                && interactive.getButtonReply() != null) {
                                            String buttonId = interactive.getButtonReply().getId();
                                            botService.processMessage(from, buttonId);
                                        } else if ("list_reply".equals(interactive.getType())
                                                && interactive.getListReply() != null) {
                                            String rowId = interactive.getListReply().getId();
                                            botService.processMessage(from, rowId);
                                        }
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

        return ResponseEntity.ok("EVENT_RECEIVED");
    }

    private boolean isValidSignature(String payload, String signatureWithPrefix) {
        String appSecret = config.getAppSecret();
        if (appSecret == null || appSecret.isEmpty()) {
            logger.warn("App Secret no configurado. Saltando validación de firma.");
            return true;
        }

        if (signatureWithPrefix == null || !signatureWithPrefix.startsWith("sha256=")) {
            return false;
        }

        try {
            String signature = signatureWithPrefix.substring(7); // Quitar "sha256="

            javax.crypto.spec.SecretKeySpec signingKey = new javax.crypto.spec.SecretKeySpec(
                    appSecret.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                    "HmacSHA256");

            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
            mac.init(signingKey);

            byte[] rawHmac = mac.doFinal(payload.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            String expectedSignature = bytesToHex(rawHmac);

            return expectedSignature.equalsIgnoreCase(signature);
        } catch (Exception e) {
            logger.error("Error validando firma: {}", e.getMessage());
            return false;
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * Verifica si un mensaje ya fue procesado (deduplicación con Redis SETNX).
     * Previene respuestas duplicadas durante reintentos de Meta por cold start.
     */
    private boolean isDuplicate(String messageId) {
        if (messageId == null) {
            return false;
        }
        try {
            Boolean isNew = redisTemplate.opsForValue()
                    .setIfAbsent(DEDUP_PREFIX + messageId, "1", DEDUP_TTL);
            if (Boolean.FALSE.equals(isNew)) {
                logger.info("[Webhook] Mensaje duplicado ignorado: {}", messageId);
                return true;
            }
        } catch (Exception e) {
            logger.warn("[Webhook] Error en deduplicación, procesando mensaje: {}", e.getMessage());
        }
        return false;
    }
}
