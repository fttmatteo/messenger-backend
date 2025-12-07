package app.domain.services;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Service
public class OcrService {

    public String extractTextFromImage(MultipartFile file) throws Exception {
        // 1. Instanciar Tesseract
        ITesseract tesseract = new Tesseract();

        // 2. Configurar la ruta de 'tessdata'
        // System.getProperty("user.dir") obtiene la ruta raíz de tu proyecto
        // (messenger-backend/messenger)
        // Esto hace que funcione tanto en tu Mac como en un servidor sin cambiar
        // código.
        String datapath = System.getProperty("user.dir") + "/tessdata";

        File tessDataFolder = new File(datapath);
        if (!tessDataFolder.exists()) {
            throw new Exception("No se encontró la carpeta tessdata en: " + datapath);
        }

        tesseract.setDatapath(datapath);

        // 3. Configurar el idioma
        // "eng" es el estándar y funciona muy bien para letras y números de placas.
        // Si descargaste "spa.traineddata", cambia esto a "spa".
        tesseract.setLanguage("eng");

        // 4. Convertir MultipartFile a File (Tesseract requiere un archivo en disco)
        File convFile = convert(file);

        try {
            // 5. Ejecutar el OCR
            String result = tesseract.doOCR(convFile);

            // 6. Limpieza básica antes de retornar
            // Eliminamos saltos de línea para que sea un solo String continuo
            return result.replaceAll("\\n", " ").trim();

        } catch (TesseractException e) {
            throw new Exception("Error al leer la imagen con Tesseract: " + e.getMessage());
        } finally {
            // 7. Borrar el archivo temporal para no llenar el disco
            if (convFile.exists()) {
                convFile.delete();
            }
        }
    }

    // Método auxiliar para convertir el archivo que viene del Controller
    private File convert(MultipartFile file) throws IOException {
        File convFile = new File(System.getProperty("java.io.tmpdir") + "/" + file.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(convFile)) {
            fos.write(file.getBytes());
        }
        return convFile;
    }
}