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

    static {
        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            // Configuración explícita para que JNA encuentre libtesseract en macOS con
            // Homebrew
            String jnaPath = System.getProperty("jna.library.path", "");
            String homebrewPath = "/opt/homebrew/lib";
            String intelPath = "/usr/local/lib";

            if (jnaPath.isEmpty()) {
                System.setProperty("jna.library.path", homebrewPath + ":" + intelPath);
            } else {
                System.setProperty("jna.library.path", jnaPath + ":" + homebrewPath + ":" + intelPath);
            }
        }
    }

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

        // 2. Leer la imagen original
        BufferedImage originalImage = ImageIO.read(imageStream);
        if (originalImage == null) {
            throw new Exception("El archivo subido no es una imagen válida.");
        }

        try {
            // 3. Intento 1: Ejecutar OCR sobre la imagen PROCESADA (Escala de grises +
            // Upscaling)
            BufferedImage cleanImage = processImage(originalImage);
            String result = tesseract.doOCR(cleanImage);

            // Si el resultado está vacío o muy corto, intentamos con la imagen original
            if (result == null || result.trim().length() < 3) {
                System.out
                        .println("OCR con procesado falló (resultado vacío/corto). Intentando con imagen original...");
                String fallbackResult = tesseract.doOCR(originalImage);
                return cleanResult(fallbackResult);
            }

            return cleanResult(result);

        } catch (TesseractException e) {
            throw new Exception("Error al procesar la imagen con Tesseract: " + e.getMessage());
        }
    }

    private String cleanResult(String text) {
        if (text == null)
            return "";
        return text.replaceAll("\\n", " ").trim();
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