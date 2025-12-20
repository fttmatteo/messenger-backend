package app.adapter.out.ocr;

import app.domain.ports.OcrPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import java.io.File;

/**
 * Mock adapter de OCR para desarrollo sin Google Vision.
 */
@Component
@ConditionalOnProperty(name = "app.ocr.mode", havingValue = "mock", matchIfMissing = true)
public class MockOcrAdapter implements OcrPort {

    @Override
    public String extractText(File imageFile) {
        String mockPlate = "ABC123";
        return mockPlate;
    }
}
