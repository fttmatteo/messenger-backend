package app.adapter.out.ocr;

import app.domain.ports.OcrPort;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;

@Component
public class TesseractAdapter implements OcrPort {

    @Override
    public String extractText(InputStream imageStream) throws Exception {
        // 1. Configurar Tesseract
        ITesseract tesseract = new Tesseract();
        String datapath = System.getProperty("user.dir") + "/tessdata";

        File tessDataFolder = new File(datapath);
        if (!tessDataFolder.exists()) {
            throw new Exception("ERROR CONFIGURACIÓN: No se encontró la carpeta tessdata en: " + datapath);
        }

        tesseract.setDatapath(datapath);
        tesseract.setLanguage("eng"); // Funciona bien para placas

        // 2. Pre-procesamiento de imagen (Limpieza)
        BufferedImage originalImage = ImageIO.read(imageStream);
        if (originalImage == null) {
            throw new Exception("El archivo subido no es una imagen válida.");
        }

        // Aplicamos filtros para mejorar la lectura
        BufferedImage cleanImage = processImage(originalImage);

        try {
            // 3. Ejecutar OCR sobre la imagen limpia
            String result = tesseract.doOCR(cleanImage);

            // 4. Limpieza del texto resultante
            return result.replaceAll("\\n", " ").trim();

        } catch (TesseractException e) {
            throw new Exception("Error al procesar la imagen con Tesseract: " + e.getMessage());
        }
    }

    /**
     * MÉTODO DE LIMPIEZA DE IMAGEN
     * Convierte a escala de grises y aumenta tamaño para mejor detección
     */
    private BufferedImage processImage(BufferedImage original) {
        // Aumentamos el tamaño (Scaling) por 2 para que las letras se vean más grandes
        int newWidth = original.getWidth() * 2;
        int newHeight = original.getHeight() * 2;

        BufferedImage resized = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = resized.createGraphics();

        // Configuración para mejor calidad
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(original, 0, 0, newWidth, newHeight, null);
        g.dispose();

        return resized;
    }
}