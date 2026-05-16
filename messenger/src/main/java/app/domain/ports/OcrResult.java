package app.domain.ports;

/**
 * Resultado del procesamiento OCR.
 */
public record OcrResult(String text, Double score) {
    public static OcrResult empty() {
        return new OcrResult("", 0.0);
    }
}
