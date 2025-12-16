package app.infrastructure.helper;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Utilidades para manejo de archivos MultipartFile.
 * 
 * Proporciona métodos helper para convertir archivos de Spring a archivos
 * temporales del sistema, con detección inteligente de extensiones.
 */
@Component
public class FileHelper {

    /**
     * Convierte un MultipartFile a File temporal con detección inteligente de
     * extensión.
     * 
     * Este método usa múltiples estrategias para determinar la extensión correcta:
     * 1. Del nombre original del archivo
     * 2. Del Content-Type HTTP
     * 3. De la firma de bytes (magic numbers) para PNG, JPEG, PDF
     * 
     * El archivo temporal creado debe ser eliminado manualmente cuando ya no se
     * necesite.
     * 
     * @param multipartFile Archivo recibido en el request HTTP
     * @return File temporal con la extensión correcta
     * @throws IOException Si hay un error al crear el archivo temporal o transferir
     *                     datos
     */
    public File convertToFile(MultipartFile multipartFile) throws IOException {
        String originalName = multipartFile.getOriginalFilename();
        String extension = "";

        // Intento 1: Obtener extensión del nombre original
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf("."));
        }

        // Intento 2: Obtener extensión del Content-Type
        if (extension.isEmpty()) {
            String contentType = multipartFile.getContentType();
            if (contentType != null) {
                extension = getExtensionFromContentType(contentType);
            }
        }

        // Intento 3: Detectar extensión de la firma de bytes (magic numbers)
        if (extension.isEmpty() || ".bin".equals(extension)) {
            extension = detectExtensionFromBytes(multipartFile);
        }

        // Fallback: Si todo falla, usar .tmp
        if (extension.isEmpty()) {
            extension = ".tmp";
        }

        // Crear archivo temporal con la extensión correcta
        File tempFile = File.createTempFile("upload-", extension);
        multipartFile.transferTo(tempFile);
        return tempFile;
    }

    /**
     * Obtiene la extensión de archivo a partir del Content-Type HTTP.
     * 
     * @param contentType Content-Type del archivo (ej: "image/jpeg")
     * @return Extensión del archivo con punto (ej: ".jpeg"), o cadena vacía si no
     *         se reconoce
     */
    private String getExtensionFromContentType(String contentType) {
        return switch (contentType) {
            case "image/jpeg", "image/jpg" -> ".jpeg";
            case "image/png" -> ".png";
            case "application/pdf" -> ".pdf";
            default -> "";
        };
    }

    /**
     * Detecta la extensión del archivo leyendo su firma de bytes (magic numbers).
     * 
     * Soporta detección de:
     * - PNG: 89 50 4E 47
     * - JPEG: FF D8 FF
     * - PDF: 25 50 44 46
     * 
     * @param multipartFile Archivo a analizar
     * @return Extensión detectada con punto (ej: ".png"), o cadena vacía si no se
     *         reconoce
     */
    private String detectExtensionFromBytes(MultipartFile multipartFile) {
        try (InputStream is = multipartFile.getInputStream()) {
            byte[] header = new byte[8];
            int read = is.read(header);

            if (read >= 4) {
                // PNG signature: 89 50 4E 47
                if (header[0] == (byte) 0x89 && header[1] == (byte) 0x50 &&
                        header[2] == (byte) 0x4E && header[3] == (byte) 0x47) {
                    return ".png";
                }

                // JPEG signature: FF D8 FF
                if (header[0] == (byte) 0xFF && header[1] == (byte) 0xD8 && header[2] == (byte) 0xFF) {
                    return ".jpeg";
                }

                // PDF signature: 25 50 44 46 (%PDF)
                if (header[0] == (byte) 0x25 && header[1] == (byte) 0x50 &&
                        header[2] == (byte) 0x44 && header[3] == (byte) 0x46) {
                    return ".pdf";
                }
            }
        } catch (IOException e) {
            // Silently fail - will use empty extension and fallback to .tmp
        }

        return "";
    }

    /**
     * Ejecuta una operación con un archivo temporal creado desde MultipartFile.
     * Garantiza que el archivo temporal se elimine automáticamente después de su
     * uso.
     * 
     * Este método sigue el patrón try-with-resources para garantizar limpieza
     * automática.
     * Es útil para operaciones que necesitan un archivo temporal y quieren asegurar
     * que nunca queden archivos huérfanos en el sistema.
     * 
     * Ejemplo de uso:
     * 
     * <pre>
     * String result = fileHelper.withTempFile(multipartFile, tempFile -> {
     *     // Usar tempFile aquí
     *     return someService.process(tempFile);
     * });
     * // tempFile se elimina automáticamente aquí
     * </pre>
     * 
     * @param <T>           Tipo del resultado de la operación
     * @param multipartFile Archivo multipart a convertir
     * @param operation     Función que procesa el archivo temporal y retorna un
     *                      resultado
     * @return El resultado de la operación
     * @throws IOException Si hay error al crear o procesar el archivo
     */
    public <T> T withTempFile(MultipartFile multipartFile, FileOperation<T> operation) throws IOException {
        File tempFile = convertToFile(multipartFile);
        try {
            return operation.execute(tempFile);
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Error processing temporary file", e);
        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    /**
     * Interfaz funcional para operaciones que procesan un archivo temporal.
     * 
     * @param <T> Tipo del resultado de la operación
     */
    @FunctionalInterface
    public interface FileOperation<T> {
        /**
         * Ejecuta la operación con el archivo temporal.
         * 
         * @param tempFile Archivo temporal a procesar
         * @return Resultado de la operación
         * @throws Exception Si hay error durante el procesamiento
         */
        T execute(File tempFile) throws Exception;
    }
}
