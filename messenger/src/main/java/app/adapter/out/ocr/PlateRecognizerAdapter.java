package app.adapter.out.ocr;

import app.domain.ports.OcrPort;
import app.domain.ports.OcrResult;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import java.io.File;

/**
 * Adapter de Plate Recognizer para OCR especializado en placas vehiculares.
 * Servicio especializado con alta precisión para placas colombianas.
 * 
 * @see <a href="https://platerecognizer.com/">Plate Recognizer</a>
 */
@Component
@ConditionalOnProperty(name = "app.ocr.mode", havingValue = "plate-recognizer")
public class PlateRecognizerAdapter implements OcrPort {

    private static final Logger logger = LoggerFactory.getLogger(PlateRecognizerAdapter.class);
    private static final String API_URL = "https://api.platerecognizer.com/v1/vin/reader/";

    private final RestTemplate restTemplate;
    private final String apiToken;

    public PlateRecognizerAdapter(
            @Value("${app.plate-recognizer.token}") String apiToken) {
        this.restTemplate = new RestTemplate();
        this.apiToken = apiToken;
    }

    @Override
    public OcrResult extractText(File imageFile) {
        try {

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.set("Authorization", "Token " + apiToken);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("upload", new FileSystemResource(imageFile));

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<VinRecognizerResponse> response = restTemplate.exchange(
                    API_URL,
                    HttpMethod.POST,
                    requestEntity,
                    VinRecognizerResponse.class);

            VinRecognizerResponse result = response.getBody();
            if (result == null || result.getVin() == null || result.getVin().isEmpty()) {
                logger.warn("El lector no detectó ningún número de Chasis en la imagen");
                return OcrResult.empty();
            }

            String vin = result.getVin().toUpperCase();
            double confidence = result.getScore();

            logger.info("Chasis detectado: {} (confianza: {}%)",
                    vin, String.format("%.2f", confidence * 100));

            if (isValidVin(vin)) {
                return new OcrResult(vin, confidence);
            }

            String cleaned = cleanVin(vin);
            if (isValidVin(cleaned)) {
                return new OcrResult(cleaned, confidence);
            }

            logger.warn("Chasis detectado no cumple formato estándar: {}", vin);
            return new OcrResult(vin, confidence);

        } catch (Exception e) {
            logger.error("Error procesando imagen de Chasis: {}", e.getMessage(), e);
            throw new RuntimeException("Error al procesar imagen de Chasis con Plate Recognizer", e);
        }
    }

    private boolean isValidVin(String vin) {
        if (vin == null) return false;
        return vin.matches("^[A-Z0-9]{10,20}$");
    }

    private String cleanVin(String vin) {
        return vin.toUpperCase()
                .replaceAll("[^A-Z0-9]", "")
                .trim();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VinRecognizerResponse {
        @JsonProperty("vin")
        private String vin;

        @JsonProperty("score")
        private Double score;

        @JsonProperty("timestamp")
        private String timestamp;

        public String getVin() {
            return vin;
        }

        public void setVin(String vin) {
            this.vin = vin;
        }

        public Double getScore() {
            return score != null ? score : 0.0;
        }

        public void setScore(Double score) {
            this.score = score;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }
    }
}
