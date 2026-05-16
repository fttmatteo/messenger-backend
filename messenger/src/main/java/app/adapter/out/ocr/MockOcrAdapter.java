package app.adapter.out.ocr;

import app.domain.ports.OcrPort;
import app.domain.ports.OcrResult;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import java.io.File;

/**
 * Mock adapter de OCR para desarrollo sin API externa.
 */
@Component
@ConditionalOnProperty(name = "app.ocr.mode", havingValue = "mock", matchIfMissing = true)
public class MockOcrAdapter implements OcrPort {

    /**
     * Simula la extracción de texto devolviendo un chasis fijo para pruebas.
     */
    @Override
    public OcrResult extractText(File imageFile) {
        String mockChasis = "CHASIS1234567890";
        return new OcrResult(mockChasis, 1.0);
    }
}
