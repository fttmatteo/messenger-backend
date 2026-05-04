package app.domain.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FileValidationService Tests")
class FileValidationServiceTest {

    private final FileValidationService fileValidationService = new FileValidationService();

    private byte[] createValidImage(String format, int width, int height) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, format, baos);
        return baos.toByteArray();
    }

    @Test
    @DisplayName("Validar imagen WebP válida debe pasar")
    void testValidateValidWebpImage() throws IOException {
        byte[] imageBytes = createValidImage("png", 800, 600); 
        MultipartFile file = new MockMultipartFile(
                "image",
                "test.webp",
                "image/webp",
                imageBytes);

        assertDoesNotThrow(() -> fileValidationService.validateImageFile(file));
    }

    @Test
    @DisplayName("Validar animación WebP válida debe pasar")
    void testValidateValidWebpAnimation() throws IOException {
        byte[] imageBytes = createValidImage("png", 640, 480);
        MultipartFile file = new MockMultipartFile(
                "animation",
                "anim.webp",
                "image/webp",
                imageBytes);

        assertDoesNotThrow(() -> fileValidationService.validateGifFile(file));
    }

    @Test
    @DisplayName("Validar imagen PNG válida debe pasar")
    void testValidateValidPngImage() throws IOException {
        byte[] imageBytes = createValidImage("png", 800, 600);
        MultipartFile file = new MockMultipartFile(
                "image",
                "test.png",
                "image/png",
                imageBytes);

        assertDoesNotThrow(() -> fileValidationService.validateImageFile(file));
    }

    @Test
    @DisplayName("Firma mayor a 2MB debe fallar")
    void testSignatureTooLargeShouldFail() {
        byte[] oversizedSignature = new byte[(int) (3 * 1024 * 1024)]; // 3MB > 2MB
        MultipartFile file = new MockMultipartFile(
                "signature",
                "large_sig.webp",
                "image/webp",
                oversizedSignature);

        SecurityException exception = assertThrows(
                SecurityException.class,
                () -> fileValidationService.validateSignatureFile(file));
        assertTrue(exception.getMessage().contains("demasiado grande"));
    }

    @Test
    @DisplayName("Animación mayor a 5MB debe fallar")
    void testAnimationTooLargeShouldFail() {
        byte[] oversizedAnim = new byte[(int) (6 * 1024 * 1024)]; // 6MB > 5MB
        MultipartFile file = new MockMultipartFile(
                "animation",
                "large_anim.webp",
                "image/webp",
                oversizedAnim);

        SecurityException exception = assertThrows(
                SecurityException.class,
                () -> fileValidationService.validateGifFile(file));
        assertTrue(exception.getMessage().contains("demasiado grande"));
    }

    @Test
    @DisplayName("Foto mayor a 10MB debe fallar")
    void testPhotoTooLargeShouldFail() {
        byte[] oversizedPhoto = new byte[(int) (11 * 1024 * 1024)]; // 11MB > 10MB
        MultipartFile file = new MockMultipartFile(
                "photo",
                "large_photo.webp",
                "image/webp",
                oversizedPhoto);

        SecurityException exception = assertThrows(
                SecurityException.class,
                () -> fileValidationService.validatePhotoFile(file));
        assertTrue(exception.getMessage().contains("demasiado grande"));
    }

    @Test
    @DisplayName("Verificar que GIF legacy sigue funcionando")
    void testLegacyGifShouldPass() throws IOException {
        byte[] gifBytes = createValidImage("gif", 100, 100);
        MultipartFile file = new MockMultipartFile(
                "animation",
                "legacy.gif",
                "image/gif",
                gifBytes);

        assertDoesNotThrow(() -> fileValidationService.validateGifFile(file));
    }
}
