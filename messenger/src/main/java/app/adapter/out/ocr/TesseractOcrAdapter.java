package app.adapter.out.ocr;

import app.domain.ports.OcrPort;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

@Component
public class TesseractOcrAdapter implements OcrPort {

    private String tessDataPath;
    private boolean isReady = false;

    @PostConstruct
    public void init() {
        try {
            // Configurar ruta de librerías nativas para Tesseract (Mac ARM64)
            String libPath = "/opt/homebrew/lib";
            System.setProperty("jna.library.path", libPath);

            nu.pattern.OpenCV.loadLocally();
            this.tessDataPath = copyTessDataToTempFolder();
            this.isReady = true;
            System.out.println("TesseractOcrAdapter inicializado correctamente. Path: " + tessDataPath);
            System.out.println("JNA library path: " + libPath);
        } catch (Exception e) {
            System.err.println("Advertencia: No se pudo inicializar el motor OCR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public String extractText(File imageFile) {
        if (!isReady) {
            throw new RuntimeException("El servicio de OCR no está disponible (Fallo en inicialización).");
        }

        File processedImage = null;
        try {
            System.out.println("=== INICIO OCR ===");
            System.out.println("Archivo entrada: " + imageFile.getAbsolutePath());

            // Cargar imagen con OpenCV
            Mat src = Imgcodecs.imread(imageFile.getAbsolutePath());
            if (src.empty()) {
                throw new RuntimeException("No se pudo cargar la imagen con OpenCV");
            }
            System.out.println("Imagen cargada: " + src.width() + "x" + src.height());

            // NUEVA FUNCIONALIDAD: Detectar y recortar placa automáticamente
            Mat plateRegion = detectAndCropPlate(src);
            Mat toProcess = plateRegion != null ? plateRegion : src;

            if (plateRegion != null) {
                System.out
                        .println("✓ Placa detectada automáticamente: " + toProcess.width() + "x" + toProcess.height());
            } else {
                System.out.println("⚠ No se detectó placa amarilla, usando imagen completa");
            }
            Mat gray = new Mat();
            Imgproc.cvtColor(toProcess, gray, Imgproc.COLOR_BGR2GRAY);

            // Redimensionar si es muy grande
            if (gray.width() > 1000) {
                double scale = 1000.0 / gray.width();
                Mat resized = new Mat();
                Imgproc.resize(gray, resized, new org.opencv.core.Size(), scale, scale, Imgproc.INTER_CUBIC);
                gray = resized;
                System.out.println("Imagen redimensionada a: " + gray.width() + "x" + gray.height());
            }

            // Mejorar contraste
            Mat equalized = new Mat();
            Imgproc.equalizeHist(gray, equalized);

            // Threshold Otsu INVERTIDO (para placas amarillas con texto negro)
            Mat binary = new Mat();
            Imgproc.threshold(equalized, binary, 0, 255, Imgproc.THRESH_BINARY_INV + Imgproc.THRESH_OTSU);

            // Operación morfológica para conectar caracteres fragmentados
            Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new org.opencv.core.Size(2, 2));
            Mat morph = new Mat();
            Imgproc.morphologyEx(binary, morph, Imgproc.MORPH_CLOSE, kernel);

            // Guardar imagen procesada en ubicación fija para debug
            String debugPath = "/Users/Matteo/Desktop/messenger-backend/messenger/uploads/debug_processed.png";
            Imgcodecs.imwrite(debugPath, morph);
            System.out.println("Imagen procesada guardada en: " + debugPath);

            // Guardar también temporal
            processedImage = File.createTempFile("processed_plate_", ".png");
            Imgcodecs.imwrite(processedImage.getAbsolutePath(), morph);

            // Configurar Tesseract
            ITesseract tesseract = new Tesseract();
            tesseract.setDatapath(this.tessDataPath);
            tesseract.setLanguage("eng");
            tesseract.setVariable("tessedit_char_whitelist", "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789");

            // Probar con diferentes Page Segmentation Modes
            String result = "";

            // Intento 1: PSM 8 (palabra única) - funcionó mejor antes
            tesseract.setPageSegMode(8);
            tesseract.setOcrEngineMode(1);
            result = tesseract.doOCR(processedImage);
            System.out.println("PSM 8 resultado: '" + (result != null ? result.trim() : "") + "'");

            // Si PSM 8 falla, probar PSM 7 (línea única)
            if (result == null || result.trim().isEmpty()) {
                tesseract.setPageSegMode(7);
                result = tesseract.doOCR(processedImage);
                System.out.println("PSM 7 resultado: '" + (result != null ? result.trim() : "") + "'");
            }

            // Si aún falla, probar PSM 6 (bloque uniforme)
            if (result == null || result.trim().isEmpty()) {
                tesseract.setPageSegMode(6);
                result = tesseract.doOCR(processedImage);
                System.out.println("PSM 6 resultado: '" + (result != null ? result.trim() : "") + "'");
            }

            // Limpiar resultado (corregir O→0, I→1, etc.)
            String cleaned = cleanPlateNumber(result);

            // Validar formato colombiano
            boolean isValid = validatePlateFormat(cleaned);

            System.out.println("OCR Result FINAL: '" + cleaned + "'");
            System.out.println("Longitud: " + cleaned.length());
            System.out.println("Formato válido: " + (isValid ? "✓ SÍ" : "✗ NO"));

            if (!isValid && !cleaned.isEmpty()) {
                System.out.println("⚠ Advertencia: La placa no coincide con formatos colombianos estándar");
            }

            System.out.println("=== FIN OCR ===");

            return cleaned;

        } catch (Exception e) {
            System.err.println("ERROR EN OCR: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error al procesar la imagen con OCR", e);
        } finally {
            if (processedImage != null && processedImage.exists()) {
                processedImage.delete();
            }
        }
    }

    /**
     * Detecta y recorta automáticamente la región de la placa amarilla
     */
    private Mat detectAndCropPlate(Mat src) {
        try {
            // Convertir a HSV para detectar color amarillo
            Mat hsv = new Mat();
            Imgproc.cvtColor(src, hsv, Imgproc.COLOR_BGR2HSV);

            // Rango de color amarillo en HSV (placas colombianas)
            org.opencv.core.Scalar lowerYellow = new org.opencv.core.Scalar(20, 100, 100);
            org.opencv.core.Scalar upperYellow = new org.opencv.core.Scalar(30, 255, 255);

            // Crear máscara de píxeles amarillos
            Mat mask = new Mat();
            org.opencv.core.Core.inRange(hsv, lowerYellow, upperYellow, mask);

            // Encontrar contornos
            java.util.List<org.opencv.core.MatOfPoint> contours = new java.util.ArrayList<>();
            Mat hierarchy = new Mat();
            Imgproc.findContours(mask, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

            // Buscar el contorno más grande (probablemente la placa)
            double maxArea = 0;
            org.opencv.core.Rect bestRect = null;

            for (org.opencv.core.MatOfPoint contour : contours) {
                double area = Imgproc.contourArea(contour);
                if (area > maxArea && area > 1000) { // Mínimo 1000 píxeles
                    org.opencv.core.Rect rect = Imgproc.boundingRect(contour);
                    // Filtrar por aspecto ratio (placas suelen ser ~2:1 o 3:1)
                    double aspectRatio = (double) rect.width / rect.height;
                    if (aspectRatio > 1.5 && aspectRatio < 4.5) {
                        maxArea = area;
                        bestRect = rect;
                    }
                }
            }

            // Si encontró placa, recortar
            if (bestRect != null) {
                // Agregar margen del 10%
                int margin = (int) (bestRect.width * 0.1);
                int x = Math.max(0, bestRect.x - margin);
                int y = Math.max(0, bestRect.y - margin);
                int width = Math.min(src.width() - x, bestRect.width + 2 * margin);
                int height = Math.min(src.height() - y, bestRect.height + 2 * margin);

                org.opencv.core.Rect expandedRect = new org.opencv.core.Rect(x, y, width, height);
                return new Mat(src, expandedRect);
            }

            return null;
        } catch (Exception e) {
            System.err.println("Error al detectar placa: " + e.getMessage());
            return null;
        }
    }

    /**
     * Limpia y valida el número de placa detectado por OCR
     */
    private String cleanPlateNumber(String rawPlate) {
        if (rawPlate == null || rawPlate.isEmpty()) {
            return rawPlate;
        }

        String cleaned = rawPlate.toUpperCase()
                .replaceAll("O", "0") // O → 0
                .replaceAll("I", "1") // I → 1
                .replaceAll("Z", "2") // Z → 2 (si está al principio o final)
                .replaceAll("S", "5") // S → 5 (contexto numérico)
                .replaceAll("[^A-Z0-9]", ""); // Eliminar todo excepto letras y números

        return cleaned;
    }

    /**
     * Valida formatos de placa colombiana:
     * - ABC123 (carros)
     * - ABC12D (motos)
     * - 123ABC (antigua)
     */
    private boolean validatePlateFormat(String plate) {
        if (plate == null || plate.length() < 5 || plate.length() > 6) {
            return false;
        }

        // Formato carro: ABC123 (3 letras, 3 números)
        boolean formatCar = plate.matches("^[A-Z]{3}[0-9]{3}$");

        // Formato moto: ABC12D (3 letras, 2 números, 1 letra)
        boolean formatMoto = plate.matches("^[A-Z]{3}[0-9]{2}[A-Z]$");

        // Formato antiguo: 123ABC (3 números, 3 letras)
        boolean formatOld = plate.matches("^[0-9]{3}[A-Z]{3}$");

        return formatCar || formatMoto || formatOld;
    }

    private String copyTessDataToTempFolder() throws IOException {
        File tempDir = Files.createTempDirectory("ocr_tessdata_").toFile();
        File tessDir = new File(tempDir, "tessdata");
        if (!tessDir.exists() && !tessDir.mkdirs()) {
            throw new IOException("No se pudo crear el directorio temporal para tessdata");
        }

        String trainFile = "eng.traineddata";
        ClassPathResource resource = new ClassPathResource("tessdata/" + trainFile);
        if (!resource.exists()) {
            throw new IOException("Archivo de entrenamiento no encontrado en resources: tessdata/" + trainFile);
        }

        File targetFile = new File(tessDir, trainFile);
        try (InputStream inputStream = resource.getInputStream()) {
            Files.copy(inputStream, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        return tessDir.getAbsolutePath();
    }
}