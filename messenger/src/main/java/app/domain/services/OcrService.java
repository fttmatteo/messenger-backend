package app.domain.services;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import javax.imageio.ImageIO;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Service;
import net.sourceforge.tess4j.Tesseract;
import app.application.exceptions.BusinessException;
import nu.pattern.OpenCV;

@Service
public class OcrService {

    static {
        // Carga librerías nativas de OpenCV
        OpenCV.loadShared();
    }

    public String extractText(String imagePath) throws Exception {
        try {
            // 1. Cargar imagen con OpenCV
            Mat src = Imgcodecs.imread(imagePath);
            if (src.empty()) {
                throw new BusinessException("No se pudo cargar la imagen: " + imagePath);
            }

            // 2. Pre-procesamiento: Escala de grises y Binarización
            Mat gray = new Mat();
            Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);

            Mat binary = new Mat();
            Imgproc.threshold(gray, binary, 0, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);

            // 3. Convertir a BufferedImage para Tesseract
            MatOfByte mob = new MatOfByte();
            Imgcodecs.imencode(".png", binary, mob);
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(mob.toArray()));

            // 4. Reconocimiento con Tesseract
            Tesseract tesseract = new Tesseract();
            // Asegúrate de configurar el datapath si no está en la raíz por defecto
            // tesseract.setDatapath("/path/to/tessdata");
            tesseract.setLanguage("spa");

            String result = tesseract.doOCR(image);

            // Limpieza básica
            return result.replaceAll("[^a-zA-Z0-9 ]", "").trim().toUpperCase();

        } catch (Exception e) {
            throw new BusinessException("Error en el proceso de OCR: " + e.getMessage());
        }
    }
}