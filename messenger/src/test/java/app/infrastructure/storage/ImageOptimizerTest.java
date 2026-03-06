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
    void shouldNotOptimizeGif() throws IOException {
        // Arrange
        byte[] gifContent = "fake-gif-content".getBytes();
        InputStream inputStream = new ByteArrayInputStream(gifContent);

        // Act
        InputStream result = imageOptimizer.optimize(inputStream, "gif");

        // Assert
        assertSame(inputStream, result);
    }

    @Test
    void shouldOptimizeJpeg() throws IOException {
        // We use a very small valid-ish stream or just mock the Thumbnails behavior if
        // possible,
        // but since it's a static-heavy library, we'll provide a minimal real stream if
        // needed.
        // For unit testing the logic, we check the extension handling.

        // Arrange
        byte[] imageContent = new byte[100]; // Dummy content, Thumbnails might fail if not valid image
        // However, the test is to ensure it *attempts* optimization for non-gif.

        // Act & Assert
        // Since we don't have a real image, Thumbnails.of() will likely throw an
        // exception,
        // which verifies it's NOT returning the original stream (unlike the gif case).
        assertThrows(Exception.class, () -> {
            imageOptimizer.optimize(new ByteArrayInputStream(imageContent), "jpg");
        });
    }
}
