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
     * Verifica que el optimizador no optimice archivos AVIF (Passthrough).
     */
    void shouldNotOptimizeAvif() throws IOException {
        byte[] avifContent = "fake-avif-content".getBytes();
        InputStream inputStream = new ByteArrayInputStream(avifContent);

        InputStream result = imageOptimizer.optimize(inputStream, "avif", false);

        assertSame(inputStream, result, "El stream de AVIF debe devolverse sin cambios");
    }

    @Test
    /**
     * Verifica que el optimizador no optimice archivos WebP (Passthrough para evitar doble compresión).
     */
    void shouldNotOptimizeWebp() throws IOException {
        byte[] webpContent = "fake-webp-content".getBytes();
        InputStream inputStream = new ByteArrayInputStream(webpContent);

        InputStream result = imageOptimizer.optimize(inputStream, "webp", false);

        assertSame(inputStream, result, "El stream de WebP debe devolverse sin cambios para evitar doble compresión");
    }

    @Test
    /**
     * Verifica que el optimizador maneje errores de formato correctamente.
     * En caso de error de lectura, debe intentar devolver el stream original o lanzar excepción controlada.
     */
    void shouldHandleInvalidFormatsGracefully() throws IOException {
        byte[] invalidContent = new byte[10];
        InputStream inputStream = new ByteArrayInputStream(invalidContent);

        assertThrows(Exception.class, () -> {
            imageOptimizer.optimize(inputStream, "jpg", false);
        });
    }
}
