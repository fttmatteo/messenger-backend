package app.adapter.out.ocr;

import app.domain.ports.OcrPort;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapter de Google Cloud Vision para OCR de placas vehiculares.
 */
@Component
@ConditionalOnProperty(name = "app.ocr.mode", havingValue = "google-vision")
public class GoogleVisionAdapter implements OcrPort {

    private static final Logger logger = LoggerFactory.getLogger(GoogleVisionAdapter.class);

    private ImageAnnotatorClient createClient() throws IOException {
        GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
        ImageAnnotatorSettings settings = ImageAnnotatorSettings.newBuilder()
                .setCredentialsProvider(() -> credentials)
                .build();
        return ImageAnnotatorClient.create(settings);
    }

    @Override
    public String extractText(File imageFile) {
        logger.info("Iniciando extracción de texto OCR para archivo: {}", imageFile.getName());
        try {

            ByteString imgBytes = ByteString.readFrom(new FileInputStream(imageFile));
            Image img = Image.newBuilder().setContent(imgBytes).build();
            Feature feat = Feature.newBuilder()
                    .setType(Feature.Type.TEXT_DETECTION)
                    .build();
            AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                    .addFeatures(feat)
                    .setImage(img)
                    .build();
            try (ImageAnnotatorClient client = createClient()) {
                BatchAnnotateImagesResponse response = client.batchAnnotateImages(List.of(request));
                AnnotateImageResponse res = response.getResponses(0);
                if (res.hasError()) {
                    logger.error("Error en respuesta Vision API: {}", res.getError().getMessage());
                    throw new RuntimeException("Error de Vision API: " + res.getError().getMessage());
                }
                String rawText = res.getFullTextAnnotation().getText();
                logger.debug("Texto crudo extraído ({} chars)", rawText != null ? rawText.length() : 0);

                String cleanedPlate = cleanPlateNumber(rawText);
                logger.info("Placa detectada y limpiada: {}", cleanedPlate);
                return cleanedPlate;
            }
        } catch (IOException e) {
            logger.error("Error IO procesando imagen para OCR: {}", e.getMessage());
            throw new RuntimeException("Error al procesar la imagen con Vision API", e);
        }
    }

    public String extractTextFromBytes(byte[] imageBytes) throws IOException {
        ByteString imgBytes = ByteString.copyFrom(imageBytes);

        Image img = Image.newBuilder().setContent(imgBytes).build();
        Feature feat = Feature.newBuilder()
                .setType(Feature.Type.TEXT_DETECTION)
                .build();
        AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                .addFeatures(feat)
                .setImage(img)
                .build();
        try (ImageAnnotatorClient client = createClient()) {
            BatchAnnotateImagesResponse response = client.batchAnnotateImages(List.of(request));
            AnnotateImageResponse res = response.getResponses(0);

            if (res.hasError()) {
                throw new IOException("Error de Vision API: " + res.getError().getMessage());
            }

            String rawText = res.getFullTextAnnotation().getText();
            return cleanPlateNumber(rawText);
        }
    }

    public List<String> detectLabels(String imagePath) throws IOException {
        List<String> labels = new ArrayList<>();
        ByteString imgBytes = ByteString.readFrom(new FileInputStream(imagePath));
        Image img = Image.newBuilder().setContent(imgBytes).build();
        Feature feat = Feature.newBuilder()
                .setType(Feature.Type.LABEL_DETECTION)
                .setMaxResults(10)
                .build();
        AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                .addFeatures(feat)
                .setImage(img)
                .build();
        try (ImageAnnotatorClient client = createClient()) {
            BatchAnnotateImagesResponse response = client.batchAnnotateImages(List.of(request));
            AnnotateImageResponse res = response.getResponses(0);

            if (res.hasError()) {
                throw new IOException("Error de Vision API: " + res.getError().getMessage());
            }

            for (EntityAnnotation annotation : res.getLabelAnnotationsList()) {
                labels.add(annotation.getDescription() + " (" +
                        String.format("%.2f%%", annotation.getScore() * 100) + ")");
            }
        }
        return labels;
    }

    private String cleanPlateNumber(String rawPlate) {
        if (rawPlate == null || rawPlate.isEmpty()) {
            return "";
        }

        String firstLine = rawPlate.split("\\n")[0];
        String cleaned = firstLine.toUpperCase()
                .replaceAll("[^A-Z0-9]", "");
        String result = findPlatePattern(cleaned);

        if (result != null) {
            return result;
        }

        String correctedMoto = applySmartCorrection(cleaned, new boolean[] { false, false, false, true, true, false });
        result = findPlatePattern(correctedMoto);
        if (result != null && result.matches("^[A-Z]{3}[0-9]{2}[A-Z]$")) {
            return result;
        }

        String correctedCar = applySmartCorrection(cleaned, new boolean[] { false, false, false, true, true, true });
        result = findPlatePattern(correctedCar);
        if (result != null && result.matches("^[A-Z]{3}[0-9]{3}$")) {
            return result;
        }

        String correctedOld = applySmartCorrection(cleaned, new boolean[] { true, true, true, false, false, false });
        result = findPlatePattern(correctedOld);
        if (result != null && result.matches("^[0-9]{3}[A-Z]{3}$")) {
            return result;
        }

        return cleaned.length() >= 6 ? cleaned.substring(0, 6) : cleaned;
    }

    private String applySmartCorrection(String text, boolean[] isNumber) {
        if (text.length() < isNumber.length) {
            return text;
        }

        StringBuilder corrected = new StringBuilder();
        for (int i = 0; i < Math.min(text.length(), isNumber.length); i++) {
            char c = text.charAt(i);
            if (i < isNumber.length) {
                if (isNumber[i]) {
                    if (c == 'O' || c == 'Q' || c == 'D')
                        c = '0';
                    else if (c == 'I' || c == 'L')
                        c = '1';
                    else if (c == 'Z')
                        c = '2';
                    else if (c == 'S')
                        c = '5';
                    else if (c == 'B')
                        c = '8';
                    else if (c == 'G')
                        c = '6';
                } else {
                    if (c == '0')
                        c = 'Q'; // Prioritize Q over O per user request
                    else if (c == '1')
                        c = 'I';
                    else if (c == '5')
                        c = 'S';
                    else if (c == '8')
                        c = 'B';
                }
            }
            corrected.append(c);
        }

        if (text.length() > isNumber.length) {
            corrected.append(text.substring(isNumber.length));
        }

        return corrected.toString();
    }

    private String findPlatePattern(String text) {
        java.util.regex.Pattern carPattern = java.util.regex.Pattern.compile("[A-Z]{3}[0-9]{3}");
        java.util.regex.Matcher carMatcher = carPattern.matcher(text);
        if (carMatcher.find()) {
            return carMatcher.group();
        }

        java.util.regex.Pattern motoPattern = java.util.regex.Pattern.compile("[A-Z]{3}[0-9]{2}[A-Z]");
        java.util.regex.Matcher motoMatcher = motoPattern.matcher(text);
        if (motoMatcher.find()) {
            return motoMatcher.group();
        }

        java.util.regex.Pattern oldPattern = java.util.regex.Pattern.compile("[0-9]{3}[A-Z]{3}");
        java.util.regex.Matcher oldMatcher = oldPattern.matcher(text);
        if (oldMatcher.find()) {
            return oldMatcher.group();
        }

        return null;
    }
}
