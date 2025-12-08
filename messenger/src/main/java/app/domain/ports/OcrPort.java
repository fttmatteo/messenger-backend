package app.domain.ports;

import java.io.File;

public interface OcrPort {
    String extractText(File imageFile);
}