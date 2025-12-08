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
        nu.pattern.OpenCV.loadLocally();
        Mat src = Imgcodecs.imread(imageFile.getAbsolutePath());
        Mat gray = new Mat();
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);
        Mat binary = new Mat();
        Imgproc.threshold(gray, binary, 0, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);
        File processedImage = File.createTempFile("processed_plate", ".png");
        Imgcodecs.imwrite(processedImage.getAbsolutePath(), binary);
        ITesseract tesseract = new Tesseract();
        tesseract.setDatapath("src/main/resources/tessdata");
        tesseract.setLanguage("eng");
        String result = tesseract.doOCR(processedImage);
        processedImage.delete();
        return result.replaceAll("[^A-Z0-9]", " ").trim();
    }
}