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
/**
 * Clase de pruebas unitarias para FileValidationService.
 */
class FileValidationServiceTest {

        private final FileValidationService fileValidationService = new FileValidationService();

        private byte[] createValidPngImage(int width, int height) throws IOException {
                BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(image, "png", baos);
                return baos.toByteArray();
        }

        private byte[] createValidJpegImage(int width, int height) throws IOException {
                BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(image, "jpg", baos);
                return baos.toByteArray();
        }

        @Test
        @DisplayName("Validar imagen PNG válida debe pasar")
        /**
         * Verifica que una imagen PNG válida sea aceptada.
         */
        void testValidateValidPngImage() throws IOException {
                byte[] imageBytes = createValidPngImage(800, 600);
                MultipartFile file = new MockMultipartFile(
                                "image",
                                "test.png",
                                "image/png",
                                imageBytes);

                assertDoesNotThrow(() -> fileValidationService.validateImageFile(file));
        }

        @Test
        @DisplayName("Validar imagen JPEG válida debe pasar")
        /**
         * Verifica que una imagen JPEG válida sea aceptada.
         */
        void testValidateValidJpegImage() throws IOException {
                byte[] imageBytes = createValidJpegImage(1024, 768);
                MultipartFile file = new MockMultipartFile(
                                "image",
                                "test.jpg",
                                "image/jpeg",
                                imageBytes);

                assertDoesNotThrow(() -> fileValidationService.validateImageFile(file));
        }

        @Test
        @DisplayName("Archivo vacío debe fallar")
        /**
         * Verifica que un archivo vacío falle la validación.
         */
        void testEmptyFileShouldFail() {
                MultipartFile file = new MockMultipartFile(
                                "image",
                                "test.png",
                                "image/png",
                                new byte[0]);

                SecurityException exception = assertThrows(
                                SecurityException.class,
                                () -> fileValidationService.validateImageFile(file));
                assertTrue(exception.getMessage().contains("vacío"));
        }

        @Test
        @DisplayName("Archivo nulo debe fallar")
        /**
         * Verifica que un archivo nulo falle la validación.
         */
        void testNullFileShouldFail() {
                SecurityException exception = assertThrows(
                                SecurityException.class,
                                () -> fileValidationService.validateImageFile(null));
                assertNotNull(exception.getMessage());
        }

        @Test
        @DisplayName("Archivo de texto debe fallar")
        /**
         * Verifica que un archivo de texto falle la validación.
         */
        void testTextFileTypeShouldFail() {
                MultipartFile file = new MockMultipartFile(
                                "file",
                                "test.txt",
                                "text/plain",
                                "This is a text file".getBytes());

                SecurityException exception = assertThrows(
                                SecurityException.class,
                                () -> fileValidationService.validateImageFile(file));
                assertTrue(exception.getMessage().contains("no permitido"));
        }

        @Test
        @DisplayName("Archivo PDF debe fallar")
        /**
         * Verifica que un archivo PDF falle la validación.
         */
        void testPdfFileShouldFail() {
                byte[] pdfMagicBytes = new byte[] { 0x25, 0x50, 0x44, 0x46 };
                MultipartFile file = new MockMultipartFile(
                                "file",
                                "test.pdf",
                                "application/pdf",
                                pdfMagicBytes);

                SecurityException exception = assertThrows(
                                SecurityException.class,
                                () -> fileValidationService.validateImageFile(file));
                assertTrue(exception.getMessage().contains("no permitido"));
        }

        @Test
        @DisplayName("Archivo mayor al límite debe fallar")
        /**
         * Verifica que un archivo mayor al límite falle la validación.
         */
        void testFileTooLargeShouldFail() throws IOException {
                byte[] largeImage = createValidPngImage(100, 100);
                byte[] oversizedImage = new byte[(int) (11 * 1024 * 1024)];
                System.arraycopy(largeImage, 0, oversizedImage, 0, largeImage.length);

                MultipartFile file = new MockMultipartFile(
                                "image",
                                "large.png",
                                "image/png",
                                oversizedImage);

                SecurityException exception = assertThrows(
                                SecurityException.class,
                                () -> fileValidationService.validateImageFile(file));
                assertTrue(exception.getMessage().contains("demasiado grande"));
        }

        @Test
        @DisplayName("Imagen demasiado pequeña debe fallar")
        /**
         * Verifica que una imagen demasiado pequeña falle la validación.
         */
        void testImageTooSmallShouldFail() throws IOException {
                byte[] tinyImage = createValidPngImage(5, 5);
                MultipartFile file = new MockMultipartFile(
                                "image",
                                "tiny.png",
                                "image/png",
                                tinyImage);

                SecurityException exception = assertThrows(
                                SecurityException.class,
                                () -> fileValidationService.validateImageFile(file));
                assertTrue(exception.getMessage().contains("pequeñas"));
        }

        @Test
        @DisplayName("Imagen más grande que el límite debe fallar")
        /**
         * Verifica que una imagen más grande que el límite falle la validación.
         */
        void testImageExceedsDimensionLimitShouldFail() throws IOException {
                byte[] largeImage = createValidPngImage(5000, 5000);
                MultipartFile file = new MockMultipartFile(
                                "image",
                                "large.png",
                                "image/png",
                                largeImage);

                SecurityException exception = assertThrows(
                                SecurityException.class,
                                () -> fileValidationService.validateImageFile(file));
                assertTrue(exception.getMessage().contains("exceden"));
        }

        @Test
        @DisplayName("Imagen con aspecto muy extremo debe fallar")
        /**
         * Verifica que una imagen con aspecto muy extremo falle la validación.
         */
        void testImageExtremAspectRatioShouldFail() throws IOException {
                byte[] extremeImage = createValidPngImage(4000, 10);
                MultipartFile file = new MockMultipartFile(
                                "image",
                                "extreme.png",
                                "image/png",
                                extremeImage);

                SecurityException exception = assertThrows(
                                SecurityException.class,
                                () -> fileValidationService.validateImageFile(file));
                assertTrue(exception.getMessage().contains("Aspecto"));
        }

        @Test
        @DisplayName("Firma válida debe pasar")
        /**
         * Verifica que una firma válida sea aceptada.
         */
        void testValidSignatureShouldPass() throws IOException {
                byte[] signatureBytes = createValidPngImage(200, 100);
                MultipartFile file = new MockMultipartFile(
                                "signature",
                                "signature.png",
                                "image/png",
                                signatureBytes);

                assertDoesNotThrow(() -> fileValidationService.validateSignatureFile(file));
        }

        @Test
        @DisplayName("Firma demasiado grande debe fallar")
        /**
         * Verifica que una firma demasiado grande falle la validación.
         */
        void testSignatureTooLargeShouldFail() {
                byte[] oversizedSignature = new byte[(int) (6 * 1024 * 1024)];
                MultipartFile file = new MockMultipartFile(
                                "signature",
                                "large_sig.png",
                                "image/png",
                                oversizedSignature);

                SecurityException exception = assertThrows(
                                SecurityException.class,
                                () -> fileValidationService.validateSignatureFile(file));
                assertTrue(exception.getMessage().contains("demasiado grande"));
        }

        @Test
        @DisplayName("Foto válida debe pasar")
        /**
         * Verifica que una foto válida sea aceptada.
         */
        void testValidPhotoShouldPass() throws IOException {
                byte[] photoBytes = createValidJpegImage(1920, 1080);
                MultipartFile file = new MockMultipartFile(
                                "photo",
                                "evidence.jpg",
                                "image/jpeg",
                                photoBytes);

                assertDoesNotThrow(() -> fileValidationService.validatePhotoFile(file));
        }

        @Test
        @DisplayName("Foto demasiado grande debe fallar")
        /**
         * Verifica que una foto demasiado grande falle la validación.
         */
        void testPhotoTooLargeShouldFail() {
                byte[] oversizedPhoto = new byte[(int) (21 * 1024 * 1024)];
                MultipartFile file = new MockMultipartFile(
                                "photo",
                                "large_photo.jpg",
                                "image/jpeg",
                                oversizedPhoto);

                SecurityException exception = assertThrows(
                                SecurityException.class,
                                () -> fileValidationService.validatePhotoFile(file));
                assertTrue(exception.getMessage().contains("demasiado grande"));
        }

        @Test
        @DisplayName("Detectar MIME type por magic bytes (PNG)")
        /**
         * Verifica que el MIME type sea detectado correctamente por magic bytes.
         */
        void testDetectPngByMagicBytes() throws IOException {
                byte[] validPng = createValidPngImage(100, 100);

                MultipartFile file = new MockMultipartFile(
                                "image",
                                "test.png",
                                "application/octet-stream",
                                validPng);

                assertDoesNotThrow(() -> fileValidationService.validateImageFile(file));
        }

        @Test
        @DisplayName("Verificar límites de validación están correctamente definidos")
        /**
         * Verifica que los límites de validación estén correctamente definidos.
         */
        void testValidationLimitsAreDefined() {
                assertEquals(10, FileValidationService.ValidationLimits.MAX_IMAGE_SIZE_MB);
                assertEquals(5, FileValidationService.ValidationLimits.MAX_SIGNATURE_SIZE_MB);
                assertEquals(20, FileValidationService.ValidationLimits.MAX_PHOTO_SIZE_MB);
                assertEquals(4096, FileValidationService.ValidationLimits.MAX_DIMENSION);
        }
}
