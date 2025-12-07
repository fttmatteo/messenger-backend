package app.adapter.out.ocr;

import app.domain.ports.OcrPort;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.stereotype.Component;
import net.sourceforge.tess4j.util.ImageHelper;
import javax.imageio.ImageIO;
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

    @SuppressWarnings("deprecation")
    @Override
    public String extractText(InputStream imageStream) throws Exception {
        ITesseract tesseract = new Tesseract();
        String datapath = System.getProperty("user.dir") + "/tessdata";

        File tessDataFolder = new File(datapath);
        if (!tessDataFolder.exists()) {
            throw new Exception("ERROR CONFIGURACIÓN: No se encontró tessdata en: " + datapath);
        }

        tesseract.setDatapath(datapath);
        tesseract.setLanguage("eng");

        // --- CAMBIO 1: WHITELIST (LISTA BLANCA) ---
        // Esto obliga a Tesseract a ignorar símbolos como '~', '=', '.', etc.
        // Solo reconocerá letras mayúsculas y números.
        tesseract.setTessVariable("tessedit_char_whitelist", "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789");

        BufferedImage originalImage = ImageIO.read(imageStream);
        if (originalImage == null) {
            throw new Exception("El archivo subido no es una imagen válida.");
        }

        try {
            // Procesado mejorado (Binarización)
            BufferedImage cleanImage = processImage(originalImage);
            String result = tesseract.doOCR(cleanImage);

            // Validación de fallback
            if (result == null || result.trim().length() < 3) {
                System.out.println("OCR procesado falló. Intentando con original...");
                // También aplicamos whitelist al intento original
                return cleanResult(tesseract.doOCR(originalImage));
            }

            return cleanResult(result);

        } catch (TesseractException e) {
            throw new Exception("Error Tesseract: " + e.getMessage());
        }
    }

    private String cleanResult(String text) {
        if (text == null)
            return "";
        // Reemplaza saltos de línea por espacio y quita espacios extra
        return text.replaceAll("\\n", " ").trim();
    }

    private BufferedImage processImage(BufferedImage original) {
        // 1. Escalar (Agrandar imagen x2) - Esto ya lo tenías y es correcto
        // Usamos ImageHelper de Tess4J que facilita esto
        BufferedImage scaled = ImageHelper.getScaledInstance(original, original.getWidth() * 2,
                original.getHeight() * 2);

        // 2. Convertir a Escala de Grises - Crucial para quitar ruido de color
        BufferedImage gray = ImageHelper.convertImageToGrayscale(scaled);

        // 3. --- CAMBIO 2: BINARIZACIÓN ---
        // Esto convierte todo lo que no sea negro oscuro en blanco puro.
        // Elimina el fondo amarillo, las sombras y los reflejos suaves.
        // El valor 180 es el umbral (0-255). Puedes ajustarlo entre 150 y 200.
        BufferedImage binary = ImageHelper.convertImageToBinary(gray);

        return binary;
    }
}