package app.infrastructure.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Servicio para validar tokens de Cloudflare Turnstile.
 * Realiza la verificación server-side contra la API de Cloudflare.
 */
@Service
public class TurnstileValidationService {

    private static final Logger logger = LoggerFactory.getLogger(TurnstileValidationService.class);
    private static final String TURNSTILE_VERIFY_URL = "https://challenges.cloudflare.com/turnstile/v0/siteverify";

    @Value("${turnstile.secret-key}")
    private String secretKey;

    private final RestTemplate restTemplate;

    public TurnstileValidationService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Valida un token de Turnstile contra la API de Cloudflare.
     */
    public boolean validateToken(String token) {
        if ("1x00000000000000000000000000000000AA".equals(secretKey)) {
            logger.info("Bypass de Turnstile detectado (usando clave de prueba de Cloudflare)");
            return true;
        }

        if (token == null || token.isBlank()) {
            logger.warn("Token de Turnstile vacío o nulo");
            return false;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("secret", secretKey);
            body.add("response", token);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = restTemplate.postForEntity(
                    TURNSTILE_VERIFY_URL,
                    request,
                    (Class<Map<String, Object>>) (Class<?>) Map.class);

            if (response.getBody() != null) {
                Boolean success = (Boolean) response.getBody().get("success");
                if (Boolean.TRUE.equals(success)) {
                    logger.debug("Validación de Turnstile exitosa");
                    return true;
                } else {
                    Object errorCodes = response.getBody().get("error-codes");
                    logger.warn("Validación de Turnstile fallida. Errores: {}", errorCodes);
                    return false;
                }
            }

            logger.error("Respuesta vacía de la API de Turnstile");
            return false;

        } catch (Exception e) {
            logger.error("Error al validar token de Turnstile: {}", e.getMessage());
            // En caso de error de red, podríamos retornar true para no bloquear usuarios
            // pero por seguridad, retornamos false
            return false;
        }
    }
}
