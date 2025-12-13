package app.domain.ports;

import java.io.File;

/**
 * Puerto (interfaz) para servicios de reconocimiento óptico de caracteres
 * (OCR).
 * 
 * <p>
 * Permite extraer texto de imágenes utilizando tecnologías como Google Cloud
 * Vision API.
 * </p>
 */
public interface OcrPort {
    String extractText(File imageFile);
}