package app.domain.ports;

import java.io.File;

/**
 * Puerto (interfaz) para servicios de reconocimiento óptico de caracteres
 * (OCR).
 * 
 * Permite extraer texto de imágenes utilizando tecnologías como Google Cloud
 * Vision API.
 */
public interface OcrPort {
    /**
     * Extrae texto de una imagen utilizando OCR.
     * 
     * @param imageFile Archivo de imagen a procesar.
     * @return Texto extraído de la imagen.
     */
    String extractText(File imageFile);
}