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
    String extractText(File imageFile);
}