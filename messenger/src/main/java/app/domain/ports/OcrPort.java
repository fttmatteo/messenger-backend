package app.domain.ports;

import java.io.InputStream;

public interface OcrPort {
    String extractText(InputStream imageStream) throws Exception;
}