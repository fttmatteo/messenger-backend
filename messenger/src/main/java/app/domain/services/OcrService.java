package app.domain.services;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Service;
import java.io.File;

@Service
public class OcrService {

    public String extractTextFromImage(File imageFile) throws Exception {
        // 1. Cargar la librería nativa de OpenCV
        nu.pattern.OpenCV.loadLocally();

        // 2. Pre-procesamiento con OpenCV (Mejora la precisión de Tesseract)
        Mat src = Imgcodecs.imread(imageFile.getAbsolutePath());
        Mat gray = new Mat();

        // Convertir a escala de grises
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);

        // Aplicar umbralización (Binarización) para resaltar texto negro sobre blanco
        Mat binary = new Mat();
        Imgproc.threshold(gray, binary, 0, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);

        // Guardar imagen procesada temporalmente para que Tesseract la lea
        File processedImage = File.createTempFile("processed_plate", ".png");
        Imgcodecs.imwrite(processedImage.getAbsolutePath(), binary);

        // 3. Reconocimiento con Tesseract
        ITesseract tesseract = new Tesseract();
        // Asegúrate de tener la carpeta 'tessdata' en tu proyecto o configura la ruta:
        tesseract.setDatapath("src/main/resources/tessdata");
        tesseract.setLanguage("eng"); // Las placas suelen usar caracteres latinos estándar

        String result = tesseract.doOCR(processedImage);

        // Limpiar archivos temporales
        processedImage.delete();

        // 4. Limpieza del resultado (Quitar espacios extra, saltos de línea)
        return result.replaceAll("[^A-Z0-9]", " ").trim();
    }
}