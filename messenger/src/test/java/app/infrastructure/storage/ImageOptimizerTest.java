package app.infrastructure.storage;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

class ImageOptimizerTest {

    private ImageOptimizer imageOptimizer;

    @BeforeEach
    void setUp() {
        imageOptimizer = new ImageOptimizer();
    }

    @Test
    void shouldNotOptimizeAvif() throws IOException {
        byte[] avifContent = "fake-avif-content".getBytes();
        InputStream inputStream = new ByteArrayInputStream(avifContent);

        InputStream result = imageOptimizer.optimize(inputStream, "avif", false);

        assertSame(inputStream, result, "El stream de AVIF debe devolverse sin cambios");
    }

    @Test
    void shouldNotOptimizeWebp() throws IOException {
        byte[] webpContent = "fake-webp-content".getBytes();
        InputStream inputStream = new ByteArrayInputStream(webpContent);

        InputStream result = imageOptimizer.optimize(inputStream, "webp", false);

        assertSame(inputStream, result, "El stream de WebP debe devolverse sin cambios para evitar doble compresión");
    }

    @Test
    void shouldHandleInvalidFormatsGracefully() throws IOException {
        byte[] invalidContent = new byte[10];
        InputStream inputStream = new ByteArrayInputStream(invalidContent);

        assertThrows(Exception.class, () -> {
            imageOptimizer.optimize(inputStream, "jpg", false);
        });
    }
}
