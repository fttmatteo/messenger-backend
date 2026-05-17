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

@DisplayName("Pruebas unitarias de FileValidationService")
class FileValidationServiceTest {

    private final FileValidationService fileValidationService = new FileValidationService();

    private byte[] createValidImage(String format, int width, int height) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, format, baos);
        return baos.toByteArray();
    }

    @Test
    @DisplayName("Debe validar imagen WebP válida")

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
    @DisplayName("Debe validar imagen PNG válida")

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
    @DisplayName("Debe fallar si la firma es demasiado grande")

    void testSignatureTooLargeShouldFail() {
        byte[] oversizedSignature = new byte[(int) (3 * 1024 * 1024)];
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
    @DisplayName("Debe fallar si la foto es demasiado grande")

    void testPhotoTooLargeShouldFail() {
        byte[] oversizedPhoto = new byte[(int) (11 * 1024 * 1024)];
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


}
