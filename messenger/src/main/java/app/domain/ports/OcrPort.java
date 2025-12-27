package app.domain.ports;

import java.io.File;

/**
 * Puerto de salida para reconocimiento óptico de caracteres (OCR).
 */
public interface OcrPort {

    /**
     * Procesa una imagen para extraer el texto legible (ej. placa vehicular).
     */
    String extractText(File imageFile);
}