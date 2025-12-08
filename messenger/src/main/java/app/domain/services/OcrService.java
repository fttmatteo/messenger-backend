package app.domain.services;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Service;
import org.springframework.core.io.ClassPathResource;
import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

@Service
public class OcrService {

    private String tessDataPath;

    @PostConstruct
    public void init() {
        try {
            nu.pattern.OpenCV.loadLocally();
            this.tessDataPath = copyTessDataToTempFolder();
            System.out.println("OCR Service inicializado. TessData en: " + tessDataPath);
        } catch (Exception e) {
            throw new RuntimeException("Error fatal inicializando OCR Service", e);
        }
    }

    public String extractTextFromImage(File imageFile) throws Exception {
        Mat src = Imgcodecs.imread(imageFile.getAbsolutePath());
        Mat gray = new Mat();
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);
        Mat binary = new Mat();
        Imgproc.threshold(gray, binary, 0, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);
        File processedImage = File.createTempFile("processed_plate", ".png");
        Imgcodecs.imwrite(processedImage.getAbsolutePath(), binary);
        ITesseract tesseract = new Tesseract();
        tesseract.setDatapath(this.tessDataPath);
        tesseract.setLanguage("eng");

        try {
            String result = tesseract.doOCR(processedImage);
            return result.replaceAll("[^A-Z0-9]", " ").trim();
        } finally {
            if (processedImage.exists()) {
                processedImage.delete();
            }
        }
    }

    private String copyTessDataToTempFolder() throws IOException {
        File tempDir = Files.createTempDirectory("ocr_app_").toFile();
        File tessDir = new File(tempDir, "tessdata");
        if (!tessDir.exists()) {
            tessDir.mkdirs();
        }

        String trainFile = "eng.traineddata";

        ClassPathResource resource = new ClassPathResource("tessdata/" + trainFile);
        if (!resource.exists()) {
            throw new IOException("No se encontró " + trainFile + " en resources/tessdata");
        }

        File targetFile = new File(tessDir, trainFile);
        try (InputStream inputStream = resource.getInputStream()) {
            Files.copy(inputStream, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

        return tessDir.getAbsolutePath();
    }
}