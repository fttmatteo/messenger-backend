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
            nu.pattern.OpenCV.loadLocally();
            this.tessDataPath = copyTessDataToTempFolder();
            this.isReady = true;
            System.out.println("TesseractOcrAdapter inicializado correctamente. Path: " + tessDataPath);
        } catch (Exception e) {
            System.err.println("Advertencia: No se pudo inicializar el motor OCR: " + e.getMessage());
        }
    }

    @Override
    public String extractText(File imageFile) {
        if (!isReady) {
            throw new RuntimeException("El servicio de OCR no está disponible (Fallo en inicialización).");
        }

        File processedImage = null;
        try {
            Mat src = Imgcodecs.imread(imageFile.getAbsolutePath());
            if (src.empty()) {
                throw new RuntimeException("No se pudo cargar la imagen con OpenCV");
            }
            Mat gray = new Mat();
            Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);
            Mat binary = new Mat();
            Imgproc.threshold(gray, binary, 0, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);
            processedImage = File.createTempFile("processed_plate_" + System.currentTimeMillis(), ".png");
            Imgcodecs.imwrite(processedImage.getAbsolutePath(), binary);
            ITesseract tesseract = new Tesseract();
            tesseract.setDatapath(this.tessDataPath);
            tesseract.setLanguage("eng");
            tesseract.setVariable("tessedit_char_whitelist", "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789");
            String result = tesseract.doOCR(processedImage);
            return result != null ? result.trim() : "";
        } catch (Exception e) {
            throw new RuntimeException("Error al procesar la imagen con OCR", e);
        } finally {
            if (processedImage != null && processedImage.exists()) {
                processedImage.delete();
            }
        }
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