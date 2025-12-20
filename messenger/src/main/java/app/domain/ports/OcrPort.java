package app.domain.ports;

import java.io.File;

/**
 * Puerto de salida para reconocimiento óptico de caracteres (OCR).
 */
public interface OcrPort {

    String extractText(File imageFile);
}