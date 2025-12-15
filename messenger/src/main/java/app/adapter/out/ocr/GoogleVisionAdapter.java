package app.adapter.out.ocr;

import app.domain.ports.OcrPort;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
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

    /**
     * Crea un cliente de Google Vision API con autenticación mediante Application
     * Default Credentials.
     * 
     * Este método configura el cliente para usar las credenciales predeterminadas
     * de la aplicación:
     * - En desarrollo: Lee la variable GOOGLE_APPLICATION_CREDENTIALS
     * - En producción: Usa la identidad del servicio (Workload Identity)
     * 
     * @return Cliente configurado de ImageAnnotatorClient
     * @throws IOException si hay error al cargar las credenciales o crear el
     *                     cliente
     */
    private ImageAnnotatorClient createClient() throws IOException {
        // Usa Application Default Credentials (ADC).
        // En local: set env var GOOGLE_APPLICATION_CREDENTIALS=/path/to/key.json
        // En Prod: Usa la identidad del servicio (Workload Identity)
        GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();

        ImageAnnotatorSettings settings = ImageAnnotatorSettings.newBuilder()
                .setCredentialsProvider(() -> credentials)
                .build();
        return ImageAnnotatorClient.create(settings);
    }

    /**
     * Extrae y procesa el texto de una imagen de placa vehicular usando Google
     * Vision API.
     * 
     * Este método implementa el flujo completo de OCR para placas:
     * 1. Lee la imagen desde el archivo
     * 2. Envía la imagen a Google Vision API para detección de texto
     * 3. Limpia y normaliza el texto detectado
     * 4. Aplica correcciones inteligentes para caracteres ambiguos
     * 5. Valida el formato de placa colombiana
     * 
     * Incluye logging detallado del proceso para debugging.
     * 
     * @param imageFile Archivo de imagen que contiene la placa vehicular
     * @return Texto de la placa procesado y validado (formato colombiano)
     * @throws RuntimeException si hay error en la API o al procesar la imagen
     */
    @Override
    public String extractText(File imageFile) {
        try {
            System.out.println("=== INICIO OCR (Google Vision) ===");
            System.out.println("Archivo entrada: " + imageFile.getAbsolutePath());

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
     * Extrae y procesa texto de una imagen desde un array de bytes.
     * 
     * Variante del método extractText que trabaja directamente con bytes en
     * memoria,
     * útil para procesar imágenes que ya están cargadas o que vienen de una
     * petición HTTP.
     * 
     * Realiza el mismo proceso de limpieza y normalización que extractText.
     * 
     * @param imageBytes Array de bytes que representa la imagen
     * @return Texto de la placa procesado y limpio
     * @throws IOException si hay error en la API o al procesar los bytes
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
     * Detecta etiquetas y objetos en una imagen usando Google Vision API.
     * 
     * Utiliza la funcionalidad de Label Detection de Google Vision para identificar
     * objetos, escenas y conceptos en la imagen. Retorna hasta 10 etiquetas con su
     * nivel de confianza.
     * 
     * Esta funcionalidad es opcional y puede usarse para validación adicional
     * (ej. verificar que la imagen contiene un vehículo).
     * 
     * @param imagePath Ruta al archivo de imagen
     * @return Lista de etiquetas detectadas con su porcentaje de confianza
     * @throws IOException si hay error al leer la imagen o en la API
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

    /**
     * Limpia y normaliza el texto detectado de una placa vehicular.
     * 
     * Proceso de limpieza:
     * 1. Toma solo la primera línea del texto detectado
     * 2. Convierte a mayúsculas y elimina caracteres no alfanuméricos
     * 3. Busca patrones de placa válidos en el texto limpio
     * 4. Si no encuentra patrón, aplica correcciones inteligentes según formato:
     * - Motos (ABC12D): Corrige posiciones 4-5 a números
     * - Carros (ABC123): Corrige posiciones 4-6 a números
     * - Antiguas (123ABC): Corrige posiciones 1-3 a números
     * 5. Retorna la placa encontrada o los primeros 6 caracteres
     * 
     * @param rawPlate Texto crudo detectado por OCR
     * @return Placa limpia y normalizada
     */
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
     * Aplica correcciones inteligentes de caracteres ambiguos según el formato
     * esperado.
     * 
     * Corrige caracteres que pueden confundirse en OCR:
     * - O ↔ 0: Corrige según si la posición espera letra o número
     * - I ↔ 1: Corrige según si la posición espera letra o número
     * 
     * Ejemplos:
     * - "ABC1O3" con patrón [false,false,false,true,true,true] → "ABC103" (O→0 en
     * pos 5)
     * - "AB01OD" con patrón [false,false,false,true,true,false] → "ABC1OD" (0→C,
     * 1→I)
     * 
     * @param text     Texto a corregir
     * @param isNumber Array booleano indicando qué posiciones esperan números
     *                 (true) o letras (false)
     * @return Texto corregido según el patrón esperado
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

    /**
     * Busca y extrae patrones de placa válidos dentro de un texto.
     * 
     * Busca en orden de prioridad:
     * 1. Placa de carro: ABC123 (3 letras + 3 números)
     * 2. Placa de moto: ABC12D (3 letras + 2 números + 1 letra)
     * 3. Placa antigua: 123ABC (3 números + 3 letras)
     * 
     * @param text Texto donde buscar el patrón de placa
     * @return Primera placa válida encontrada, o null si no se encuentra ninguna
     */
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

    /**
     * Valida que una placa cumpla con los formatos colombianos oficiales.
     * 
     * Formatos válidos:
     * - ABC123: Carros (3 letras mayúsculas + 3 dígitos)
     * - ABC12D: Motos (3 letras mayúsculas + 2 dígitos + 1 letra mayúscula)
     * - 123ABC: Placas antiguas (3 dígitos + 3 letras mayúsculas)
     * 
     * @param plate Texto de placa a validar
     * @return true si la placa cumple con algún formato válido, false en caso
     *         contrario
     */
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
