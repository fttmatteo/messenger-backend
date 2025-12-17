package app.infrastructure.helper;

import app.application.exceptions.InputsException;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

/**
 * Utilidad para validar archivos subidos al sistema.
 * 
 * Valida:
 * - Tipo MIME del archivo (solo imágenes permitidas)
 * - Extensión del archivo
 * - Tamaño máximo
 * - Contenido no vacío
 * 
 * Seguridad:
 * - Previene subida de archivos ejecutables maliciosos
 * - Valida tanto MIME type como extensión (doble verificación)
 * - Rechaza archivos vacíos
 * 
 * @see app.adapter.in.rest.controllers.ServiceDeliveryController
 */
public class FileValidator {

    /** Tipos MIME permitidos para imágenes */
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg",
            "image/jpg",
            "image/png");

    /** Extensiones permitidas para imágenes */
    private static final List<String> ALLOWED_IMAGE_EXTENSIONS = Arrays.asList(
            "jpg", "jpeg", "png");

    /** Tamaño máximo de archivo: 10MB */
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    /**
     * Valida que un archivo sea una imagen válida.
     * 
     * Realiza las siguientes validaciones:
     * 1. Archivo no nulo y no vacío
     * 2. Tamaño dentro del límite permitido
     * 3. Tipo MIME es imagen válida
     * 4. Extensión del archivo es válida
     * 
     * @param file Archivo a validar
     * @throws InputsException si el archivo no cumple las validaciones
     */
    public static void validateImage(MultipartFile file) {
        validateFile(file, ALLOWED_IMAGE_TYPES, ALLOWED_IMAGE_EXTENSIONS, MAX_FILE_SIZE);
    }

    /**
     * Valida un archivo genérico con restricciones personalizadas.
     * 
     * @param file              Archivo a validar
     * @param allowedMimeTypes  Lista de tipos MIME permitidos
     * @param allowedExtensions Lista de extensiones permitidas
     * @param maxSize           Tamaño máximo en bytes
     * @throws InputsException si el archivo no cumple las validaciones
     */
    public static void validateFile(MultipartFile file,
            List<String> allowedMimeTypes,
            List<String> allowedExtensions,
            long maxSize) {
        // 1. Validar que el archivo no sea nulo ni vacío
        if (file == null || file.isEmpty()) {
            throw new InputsException("El archivo está vacío o no fue proporcionado");
        }

        // 2. Validar tamaño
        if (file.getSize() > maxSize) {
            throw new InputsException(
                    String.format("El archivo excede el tamaño máximo permitido (%d MB)", maxSize / (1024 * 1024)));
        }

        // 3. Validar MIME type
        String contentType = file.getContentType();
        if (contentType == null || !allowedMimeTypes.contains(contentType.toLowerCase())) {
            throw new InputsException(
                    "Tipo de archivo no permitido: " + contentType +
                            ". Tipos permitidos: " + String.join(", ", allowedMimeTypes));
        }

        // 4. Validar extensión real del archivo (no confiar solo en MIME type)
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

    /**
     * Verifica si un archivo es una imagen válida sin lanzar excepción.
     * 
     * @param file Archivo a verificar
     * @return true si es una imagen válida, false en caso contrario
     */
    public static boolean isValidImage(MultipartFile file) {
        try {
            validateImage(file);
            return true;
        } catch (InputsException e) {
            return false;
        }
    }
}
