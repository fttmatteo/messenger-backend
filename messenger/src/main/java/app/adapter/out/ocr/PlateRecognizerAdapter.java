package app.adapter.out.ocr;

import app.domain.ports.OcrPort;
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
import java.util.List;

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
    private static final String API_URL = "https://api.platerecognizer.com/v1/plate-reader/";

    private final RestTemplate restTemplate;
    private final String apiToken;
    private final String region;

    public PlateRecognizerAdapter(
            @Value("${app.plate-recognizer.token}") String apiToken,
            @Value("${app.plate-recognizer.region:co}") String region) {
        this.restTemplate = new RestTemplate();
        this.apiToken = apiToken;
        this.region = region;
        logger.info("PlateRecognizerAdapter inicializado para región: {}", region);
    }

    @Override
    public String extractText(File imageFile) {
        try {
            logger.debug("Procesando imagen con Plate Recognizer: {}", imageFile.getName());

            // Preparar headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.set("Authorization", "Token " + apiToken);

            // Preparar body multipart
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("upload", new FileSystemResource(imageFile));
            body.add("regions", region); // Colombia

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // Hacer request
            ResponseEntity<PlateRecognizerResponse> response = restTemplate.exchange(
                    API_URL,
                    HttpMethod.POST,
                    requestEntity,
                    PlateRecognizerResponse.class);

            // Procesar respuesta
            PlateRecognizerResponse result = response.getBody();
            if (result == null || result.getResults() == null || result.getResults().isEmpty()) {
                logger.warn("Plate Recognizer no detectó ninguna placa en la imagen");
                return "";
            }

            PlateResult plateResult = result.getResults().get(0);
            String plate = plateResult.getPlate().toUpperCase();
            double confidence = plateResult.getScore();
            String vehicleType = plateResult.getVehicle() != null ? plateResult.getVehicle().getType() : "unknown";

            logger.info("Placa detectada: {} (confianza: {}%, vehículo: {})",
                    plate, String.format("%.2f", confidence * 100), vehicleType);

            // Validar formato colombiano (carro, moto o motocarro)
            if (isValidColombianPlate(plate)) {
                return plate;
            }

            // Si no es válido, intentar limpiar
            String cleaned = cleanPlate(plate);
            if (isValidColombianPlate(cleaned)) {
                logger.debug("Placa limpiada: {} -> {}", plate, cleaned);
                return cleaned;
            }

            logger.warn("Placa detectada no cumple formato colombiano esperado: {}", plate);
            return plate; // Devolver de todos modos

        } catch (Exception e) {
            logger.error("Error procesando imagen con Plate Recognizer: {}", e.getMessage(), e);
            throw new RuntimeException("Error al procesar imagen con Plate Recognizer", e);
        }
    }

    /**
     * Valida si la placa cumple con formato colombiano válido.
     * Formatos soportados:
     * - Carro: ABC123 (3 letras + 3 números)
     * - Moto: ABC12D (3 letras + 2 números + 1 letra)
     * - Motocarro: 123ABC (3 números + 3 letras)
     */
    private boolean isValidColombianPlate(String plate) {
        if (plate == null || plate.length() != 6) {
            return false;
        }
        // Carro: ABC123 (3 letras + 3 números)
        boolean isCar = plate.matches("^[A-Z]{3}[0-9]{3}$");
        // Moto: ABC12D (3 letras + 2 números + 1 letra)
        boolean isMoto = plate.matches("^[A-Z]{3}[0-9]{2}[A-Z]$");
        // Motocarro: 123ABC (3 números + 3 letras)
        boolean isMotocarro = plate.matches("^[0-9]{3}[A-Z]{3}$");
        return isCar || isMoto || isMotocarro;
    }

    /**
     * Limpia la placa removiendo caracteres no válidos.
     */
    private String cleanPlate(String plate) {
        return plate.toUpperCase()
                .replaceAll("[^A-Z0-9]", "")
                .trim();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PlateRecognizerResponse {
        @JsonProperty("results")
        private List<PlateResult> results;

        @JsonProperty("processing_time")
        private Double processingTime;

        public List<PlateResult> getResults() {
            return results;
        }

        public void setResults(List<PlateResult> results) {
            this.results = results;
        }

        public Double getProcessingTime() {
            return processingTime;
        }

        public void setProcessingTime(Double processingTime) {
            this.processingTime = processingTime;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PlateResult {
        @JsonProperty("plate")
        private String plate;

        @JsonProperty("score")
        private Double score;

        @JsonProperty("region")
        private Region region;

        @JsonProperty("vehicle")
        private Vehicle vehicle;

        public String getPlate() {
            return plate;
        }

        public void setPlate(String plate) {
            this.plate = plate;
        }

        public Double getScore() {
            return score != null ? score : 0.0;
        }

        public void setScore(Double score) {
            this.score = score;
        }

        public Region getRegion() {
            return region;
        }

        public void setRegion(Region region) {
            this.region = region;
        }

        public Vehicle getVehicle() {
            return vehicle;
        }

        public void setVehicle(Vehicle vehicle) {
            this.vehicle = vehicle;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Region {
        @JsonProperty("code")
        private String code;

        @JsonProperty("score")
        private Double score;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public Double getScore() {
            return score;
        }

        public void setScore(Double score) {
            this.score = score;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Vehicle {
        @JsonProperty("type")
        private String type;

        @JsonProperty("score")
        private Double score;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Double getScore() {
            return score;
        }

        public void setScore(Double score) {
            this.score = score;
        }
    }
}
