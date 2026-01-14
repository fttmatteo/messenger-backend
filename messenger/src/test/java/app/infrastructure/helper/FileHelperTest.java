package app.infrastructure.helper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FileHelper Unit Tests")
class FileHelperTest {

    private FileHelper fileHelper;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        fileHelper = new FileHelper();
    }

    @Nested
    @DisplayName("Conversión de MultipartFile a File")
    class ConvertToFileTests {

        @Test
        @DisplayName("Debe convertir MultipartFile con extensión en nombre original")
        /**
         * Verifica la conversión preservando la extensión si está presente en el nombre
         * original.
         */
        void shouldConvertWithOriginalExtension() throws IOException {
            MockMultipartFile multipartFile = new MockMultipartFile(
                    "file",
                    "test-image.png",
                    "image/png",
                    "fake image content".getBytes());

            File result = fileHelper.convertToFile(multipartFile);

            assertNotNull(result);
            assertTrue(result.exists());
            assertTrue(result.getName().endsWith(".png"));

            result.delete();
        }

        @Test
        @DisplayName("Debe detectar extensión por Content-Type cuando no hay nombre")
        /**
         * Verifica la deducción de extensión basada en el tipo MIME.
         */
        void shouldDetectExtensionByContentType() throws IOException {
            MockMultipartFile multipartFile = new MockMultipartFile(
                    "file",
                    null,
                    "image/jpeg",
                    "fake jpeg content".getBytes());

            File result = fileHelper.convertToFile(multipartFile);

            assertNotNull(result);
            assertTrue(result.exists());
            assertTrue(result.getName().endsWith(".jpeg"));

            result.delete();
        }

        @Test
        @DisplayName("Debe detectar PNG por magic bytes")
        /**
         * Verifica detección de contenido real (PNG) inspeccionando bytes si metadatos
         * faltan.
         */
        void shouldDetectPngByMagicBytes() throws IOException {
            byte[] pngHeader = new byte[] { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A };
            MockMultipartFile multipartFile = new MockMultipartFile(
                    "file",
                    "unknown",
                    "application/octet-stream",
                    pngHeader);

            File result = fileHelper.convertToFile(multipartFile);

            assertNotNull(result);
            assertTrue(result.getName().endsWith(".png"));

            result.delete();
        }

        @Test
        @DisplayName("Debe detectar JPEG por magic bytes")
        void shouldDetectJpegByMagicBytes() throws IOException {
            byte[] jpegContent = new byte[] { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0x00 };
            MockMultipartFile multipartFile = new MockMultipartFile(
                    "file",
                    "unknown",
                    "application/octet-stream",
                    jpegContent);

            File result = fileHelper.convertToFile(multipartFile);

            assertNotNull(result);
            assertTrue(result.getName().endsWith(".jpeg"));

            result.delete();
        }

        @Test
        @DisplayName("Debe detectar PDF por magic bytes")
        void shouldDetectPdfByMagicBytes() throws IOException {
            byte[] pdfContent = new byte[] { 0x25, 0x50, 0x44, 0x46, 0x2D };
            MockMultipartFile multipartFile = new MockMultipartFile(
                    "file",
                    "unknown",
                    "application/octet-stream",
                    pdfContent);

            File result = fileHelper.convertToFile(multipartFile);

            assertNotNull(result);
            assertTrue(result.getName().endsWith(".pdf"));

            result.delete();
        }

        @Test
        @DisplayName("Debe usar .tmp cuando no puede detectar extensión")
        void shouldUseTmpWhenCannotDetectExtension() throws IOException {
            MockMultipartFile multipartFile = new MockMultipartFile(
                    "file",
                    null,
                    null,
                    "unknown content".getBytes());

            File result = fileHelper.convertToFile(multipartFile);

            assertNotNull(result);
            assertTrue(result.getName().endsWith(".tmp"));

            result.delete();
        }
    }

    @Nested
    @DisplayName("Cleanup de Archivos Temporales")
    class CleanupTests {

        @Test
        @DisplayName("Debe eliminar lista de archivos temporales")
        /**
         * Verifica la limpieza correcta de múltiples archivos temporales.
         */
        void shouldCleanupTempFiles() throws IOException {
            File file1 = File.createTempFile("test1-", ".tmp");
            File file2 = File.createTempFile("test2-", ".tmp");
            List<File> files = List.of(file1, file2);

            assertTrue(file1.exists());
            assertTrue(file2.exists());

            fileHelper.cleanupTempFiles(new ArrayList<>(files));

            assertFalse(file1.exists());
            assertFalse(file2.exists());
        }

        @Test
        @DisplayName("Debe manejar lista nula sin error")
        void shouldHandleNullList() {
            assertDoesNotThrow(() -> fileHelper.cleanupTempFiles(null));
        }

        @Test
        @DisplayName("Debe manejar lista vacía sin error")
        void shouldHandleEmptyList() {
            assertDoesNotThrow(() -> fileHelper.cleanupTempFiles(new ArrayList<>()));
        }

        @Test
        @DisplayName("Debe ignorar archivos nulos en la lista")
        void shouldIgnoreNullFilesInList() throws IOException {
            File validFile = File.createTempFile("test-", ".tmp");
            List<File> files = new ArrayList<>();
            files.add(validFile);
            files.add(null);

            assertDoesNotThrow(() -> fileHelper.cleanupTempFiles(files));
            assertFalse(validFile.exists());
        }
    }

    @Nested
    @DisplayName("withTempFile - Cleanup Automático")
    class WithTempFileTests {

        @Test
        @DisplayName("Debe ejecutar operación y limpiar archivo")
        void shouldExecuteAndCleanup() throws IOException {
            MockMultipartFile multipartFile = new MockMultipartFile(
                    "file",
                    "test.txt",
                    "text/plain",
                    "content".getBytes());

            final File[] capturedFile = new File[1];

            String result = fileHelper.withTempFile(multipartFile, tempFile -> {
                capturedFile[0] = tempFile;
                assertTrue(tempFile.exists());
                return "success";
            });

            assertEquals("success", result);
            assertFalse(capturedFile[0].exists(), "El archivo temporal debe ser eliminado");
        }

        @Test
        @DisplayName("Debe limpiar archivo incluso si operación falla")
        void shouldCleanupEvenOnException() {
            MockMultipartFile multipartFile = new MockMultipartFile(
                    "file",
                    "test.txt",
                    "text/plain",
                    "content".getBytes());

            final File[] capturedFile = new File[1];

            assertThrows(IOException.class, () -> {
                fileHelper.withTempFile(multipartFile, tempFile -> {
                    capturedFile[0] = tempFile;
                    throw new RuntimeException("Test error");
                });
            });

            assertFalse(capturedFile[0].exists(), "El archivo temporal debe ser eliminado tras error");
        }
    }

    @Nested
    @DisplayName("convertToFiles - Múltiples Archivos")
    class ConvertToFilesTests {

        @Test
        @DisplayName("Debe convertir lista de MultipartFiles")
        void shouldConvertMultipleFiles() throws IOException {
            MockMultipartFile file1 = new MockMultipartFile("file1", "a.png", "image/png", "content1".getBytes());
            MockMultipartFile file2 = new MockMultipartFile("file2", "b.png", "image/png", "content2".getBytes());
            List<MultipartFile> multipartFiles = List.of(file1, file2);

            List<File> result = fileHelper.convertToFiles(multipartFiles);

            assertEquals(2, result.size());
            assertTrue(result.get(0).exists());
            assertTrue(result.get(1).exists());

            fileHelper.cleanupTempFiles(result);
        }

        @Test
        @DisplayName("Debe retornar lista vacía para lista nula")
        void shouldReturnEmptyListForNull() throws IOException {
            List<File> result = fileHelper.convertToFiles(null);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Debe ignorar archivos vacíos")
        void shouldIgnoreEmptyFiles() throws IOException {
            MockMultipartFile validFile = new MockMultipartFile("file1", "a.png", "image/png", "content".getBytes());
            MockMultipartFile emptyFile = new MockMultipartFile("file2", "b.png", "image/png", new byte[0]);
            List<MultipartFile> multipartFiles = List.of(validFile, emptyFile);

            List<File> result = fileHelper.convertToFiles(multipartFiles);

            assertEquals(1, result.size());

            fileHelper.cleanupTempFiles(result);
        }
    }
}
