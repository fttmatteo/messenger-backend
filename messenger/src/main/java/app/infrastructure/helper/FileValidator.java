package app.infrastructure.helper;

import app.application.exceptions.InputsException;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

/**
 * Validador de archivos subidos (imágenes) por tipo y tamaño.
 */
public class FileValidator {

    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg",
            "image/jpg",
            "image/png");
    private static final List<String> ALLOWED_IMAGE_EXTENSIONS = Arrays.asList(
            "jpg", "jpeg", "png");
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    public static void validateImage(MultipartFile file) {
        validateFile(file, ALLOWED_IMAGE_TYPES, ALLOWED_IMAGE_EXTENSIONS, MAX_FILE_SIZE);
    }

    public static void validateFile(MultipartFile file,
            List<String> allowedMimeTypes,
            List<String> allowedExtensions,
            long maxSize) {
        if (file == null || file.isEmpty()) {
            throw new InputsException("El archivo está vacío o no fue proporcionado");
        }
        if (file.getSize() > maxSize) {
            throw new InputsException(
                    String.format("El archivo excede el tamaño máximo permitido (%d MB)", maxSize / (1024 * 1024)));
        }

        String contentType = file.getContentType();
        if (contentType == null || !allowedMimeTypes.contains(contentType.toLowerCase())) {
            throw new InputsException(
                    "Tipo de archivo no permitido: " + contentType +
                            ". Tipos permitidos: " + String.join(", ", allowedMimeTypes));
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new InputsException("El archivo debe tener una extensión válida");
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        if (!allowedExtensions.contains(extension)) {
            throw new InputsException(
                    "Extensión no permitida: " + extension +
                            ". Extensiones permitidas: " + String.join(", ", allowedExtensions));
        }
    }

    public static boolean isValidImage(MultipartFile file) {
        try {
            validateImage(file);
            return true;
        } catch (InputsException e) {
            return false;
        }
    }
}
