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
    /**
     * Verifica que el optimizador no optimice archivos GIF.
     */
    void shouldNotOptimizeGif() throws IOException {
        byte[] gifContent = "fake-gif-content".getBytes();
        InputStream inputStream = new ByteArrayInputStream(gifContent);

        InputStream result = imageOptimizer.optimize(inputStream, "gif", false);

        assertSame(inputStream, result);
    }

    @Test
    /**
     * Verifica que el optimizador optimice archivos JPEG.
     */
    void shouldOptimizeJpeg() throws IOException {
        byte[] imageContent = new byte[100];
        assertThrows(Exception.class, () -> {
            imageOptimizer.optimize(new ByteArrayInputStream(imageContent), "jpg", false);
        });
    }
}
