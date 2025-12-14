package app.adapter.out.ocr;

import app.domain.ports.OcrPort;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Adaptador para reconocimiento óptico de caracteres (OCR) usando Google Cloud
 * Vision.
 * Se encarga de extraer texto de imágenes y validar formatos de placas
 * detectadas.
 */
@Component
public class GoogleVisionAdapter implements OcrPort {

    @Value("${google.cloud.credentials.path:config/google-vision-credentials.json}")
    private String credentialsPath;

    private ImageAnnotatorClient createClient() throws IOException {
        File credentialsFile = new File(credentialsPath);
        if (!credentialsFile.exists()) {
            throw new IOException("Archivo de credenciales no encontrado: " + credentialsPath +
                    ". Asegúrate de que el archivo existe en: " + credentialsFile.getAbsolutePath());
        }

        GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(credentialsFile));
        ImageAnnotatorSettings settings = ImageAnnotatorSettings.newBuilder()
                .setCredentialsProvider(() -> credentials)
                .build();
        return ImageAnnotatorClient.create(settings);
    }

    /**
     * Extrae texto de una imagen usando Google Vision API
     * Implementación de OcrPort
     * 
     * @param imageFile Archivo de imagen
     * @return Texto extraído y procesado (placa colombiana)
     */
    @Override
    public String extractText(File imageFile) {
        try {
            System.out.println("=== INICIO OCR (Google Vision) ===");
            System.out.println("Archivo entrada: " + imageFile.getAbsolutePath());
            System.out.println("Credenciales: " + credentialsPath);

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
                    throw new RuntimeException("Error de Vision API: " + res.getError().getMessage());
                }

                String rawText = res.getFullTextAnnotation().getText();
                System.out.println("Texto OCR detectado: '" + rawText + "'");

                String cleanedPlate = cleanPlateNumber(rawText);
                boolean isValid = validatePlateFormat(cleanedPlate);

                System.out.println("Placa procesada: '" + cleanedPlate + "'");
                System.out.println("Formato válido: " + (isValid ? "✓ SÍ" : "✗ NO"));
                System.out.println("=== FIN OCR ===");

                return cleanedPlate;
            }
        } catch (IOException e) {
            System.err.println("ERROR EN OCR: " + e.getMessage());
            throw new RuntimeException("Error al procesar la imagen con Vision API", e);
        }
    }

    /**
     * Extrae texto de una imagen desde bytes
     * 
     * @param imageBytes Bytes de la imagen
     * @return Texto extraído
     */
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

    /**
     * Detecta etiquetas/objetos en una imagen
     * 
     * @param imagePath Ruta a la imagen
     * @return Lista de etiquetas detectadas con su confianza
     */
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

    /**
     * Aplica correcciones O↔0 e I↔1 basadas en si la posición espera número o letra
     * 
     * @param text     Texto a corregir
     * @param isNumber Array indicando qué posiciones esperan números (true) o
     *                 letras (false)
     */
    private String applySmartCorrection(String text, boolean[] isNumber) {
        if (text.length() < isNumber.length) {
            return text;
        }

        StringBuilder corrected = new StringBuilder();
        for (int i = 0; i < Math.min(text.length(), isNumber.length); i++) {
            char c = text.charAt(i);
            if (i < isNumber.length) {
                if (isNumber[i]) {
                    if (c == 'O')
                        c = '0';
                    else if (c == 'I')
                        c = '1';
                } else {
                    if (c == '0')
                        c = 'O';
                    else if (c == '1')
                        c = 'I';
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

    private boolean validatePlateFormat(String plate) {
        if (plate == null || plate.length() < 5 || plate.length() > 6) {
            return false;
        }

        boolean formatCar = plate.matches("^[A-Z]{3}[0-9]{3}$");
        boolean formatMoto = plate.matches("^[A-Z]{3}[0-9]{2}[A-Z]$");
        boolean formatOld = plate.matches("^[0-9]{3}[A-Z]{3}$");

        return formatCar || formatMoto || formatOld;
    }
}
