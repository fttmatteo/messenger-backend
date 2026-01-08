package app.domain.services;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servicio para validación robusta de archivos.
 * Realiza validaciones de:
 * - Tamaño máximo
 * - Tipo MIME real (no solo extensión)
 * - Contenido real del archivo
 * - Dimensiones de imagen
 */
@Service
public class FileValidationService {

    private static final Logger logger = LoggerFactory.getLogger(FileValidationService.class);

    // Tipos MIME permitidos
    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    // Límites de tamaño
    private static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final long MAX_SIGNATURE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final long MAX_PHOTO_SIZE = 20 * 1024 * 1024; // 20MB por foto

    // Límites de dimensiones
    private static final int MAX_IMAGE_DIMENSION = 4096; // 4096x4096

    /**
     * Valida un archivo de imagen genérico (para creación de servicios).
     * Usado para: imágenes de placas detectadas
     */
    public void validateImageFile(MultipartFile file) throws SecurityException {
        if (file == null) {
            throw new SecurityException("Archivo no proporcionado");
        }

        logger.info("Validando archivo de imagen: {}", file.getOriginalFilename());

        // 1. Verificar que no esté vacío
        if (file.isEmpty()) {
            throw new SecurityException("Archivo vacío");
        }

        // 2. Verificar tamaño
        if (file.getSize() > MAX_IMAGE_SIZE) {
            logger.warn("Archivo de imagen excede tamaño máximo: {} bytes",
                    file.getSize());
            throw new SecurityException(
                    String.format("Archivo demasiado grande. Máximo: %dMB",
                            MAX_IMAGE_SIZE / (1024 * 1024)));
        }

        // 3. Detectar MIME type REAL
        String detectedType = detectMimeType(file);
        if (!ALLOWED_IMAGE_TYPES.contains(detectedType)) {
            logger.warn("Tipo de archivo no permitido: {}", detectedType);
            throw new SecurityException("Tipo de archivo no permitido: " + detectedType);
        }

        // 4. Validar que sea realmente una imagen (leer contenido)
        validateImageContent(file);

        logger.info("Validación de imagen exitosa: {}", file.getOriginalFilename());
    }

    /**
     * Valida un archivo de firma (con validaciones más estrictas).
     * Usado para: firmas digitales en entregas
     */
    public void validateSignatureFile(MultipartFile file) throws SecurityException {
        if (file == null) {
            throw new SecurityException("Archivo de firma no proporcionado");
        }

        logger.info("Validando archivo de firma: {}", file.getOriginalFilename());

        // 1. Validaciones básicas (imagen)
        if (file.isEmpty()) {
            throw new SecurityException("Archivo de firma vacío");
        }

        if (file.getSize() > MAX_SIGNATURE_SIZE) {
            logger.warn("Archivo de firma excede tamaño máximo: {} bytes",
                    file.getSize());
            throw new SecurityException(
                    String.format("Firma demasiado grande. Máximo: %dMB",
                            MAX_SIGNATURE_SIZE / (1024 * 1024)));
        }

        // 2. Detectar MIME type
        String detectedType = detectMimeType(file);
        if (!ALLOWED_IMAGE_TYPES.contains(detectedType)) {
            logger.warn("Tipo de firma no permitido: {}", detectedType);
            throw new SecurityException("Tipo de archivo de firma no permitido");
        }

        // 3. Validar contenido de imagen
        validateImageContent(file);

        logger.info("Validación de firma exitosa: {}", file.getOriginalFilename());
    }

    /**
     * Valida archivos de fotos (evidencia).
     * Usado para: fotos de entregas/evidencia
     */
    public void validatePhotoFile(MultipartFile file) throws SecurityException {
        if (file == null) {
            throw new SecurityException("Archivo de foto no proporcionado");
        }

        logger.info("Validando archivo de foto: {}", file.getOriginalFilename());

        if (file.isEmpty()) {
            throw new SecurityException("Archivo de foto vacío");
        }

        if (file.getSize() > MAX_PHOTO_SIZE) {
            logger.warn("Archivo de foto excede tamaño máximo: {} bytes",
                    file.getSize());
            throw new SecurityException(
                    String.format("Foto demasiado grande. Máximo: %dMB",
                            MAX_PHOTO_SIZE / (1024 * 1024)));
        }

        String detectedType = detectMimeType(file);
        if (!ALLOWED_IMAGE_TYPES.contains(detectedType)) {
            logger.warn("Tipo de foto no permitido: {}", detectedType);
            throw new SecurityException("Tipo de foto no permitido");
        }

        validateImageContent(file);

        logger.info("Validación de foto exitosa: {}", file.getOriginalFilename());
    }

    /**
     * Detecta el tipo MIME real del archivo (no solo la extensión).
     */
    private String detectMimeType(MultipartFile file) throws SecurityException {
        try {
            // Intentar detectar usando el contenido del archivo
            String contentType = file.getContentType();

            // Fallback: analizar el stream si es posible
            if (contentType == null || contentType.equals("application/octet-stream")) {
                // Intentar con magic bytes
                byte[] bytes = new byte[12];
                try (var is = file.getInputStream()) {
                    is.read(bytes);
                }

                // Detectar por magic bytes
                contentType = detectByMagicBytes(bytes);
            }

            if (contentType == null) {
                throw new SecurityException("No se pudo determinar el tipo de archivo");
            }

            return contentType;
        } catch (IOException e) {
            logger.error("Error detectando tipo MIME: {}", e.getMessage());
            throw new SecurityException("Error validando archivo");
        }
    }

    /**
     * Detecta tipo MIME por magic bytes (primeros bytes del archivo).
     */
    private String detectByMagicBytes(byte[] bytes) {
        if (bytes.length < 4)
            return null;

        // PNG: 89 50 4E 47
        if (bytes[0] == (byte) 0x89 && bytes[1] == 0x50 &&
                bytes[2] == 0x4E && bytes[3] == 0x47) {
            return "image/png";
        }

        // JPEG: FF D8 FF
        if (bytes[0] == (byte) 0xFF && bytes[1] == (byte) 0xD8 &&
                bytes[2] == (byte) 0xFF) {
            return "image/jpeg";
        }

        // WebP: RIFF ... WEBP
        if (bytes[0] == 0x52 && bytes[1] == 0x49 &&
                bytes[2] == 0x46 && bytes[3] == 0x46) {
            if (bytes.length >= 12 &&
                    bytes[8] == 0x57 && bytes[9] == 0x45 &&
                    bytes[10] == 0x42 && bytes[11] == 0x50) {
                return "image/webp";
            }
        }

        return null;
    }

    /**
     * Valida que el contenido sea realmente una imagen y verifica dimensiones.
     */
    private void validateImageContent(MultipartFile file) throws SecurityException {
        try {
            BufferedImage image = ImageIO.read(file.getInputStream());

            if (image == null) {
                logger.warn("Archivo no es una imagen válida: {}", file.getOriginalFilename());
                throw new SecurityException("El archivo no es una imagen válida");
            }

            // Verificar dimensiones
            if (image.getWidth() > MAX_IMAGE_DIMENSION ||
                    image.getHeight() > MAX_IMAGE_DIMENSION) {
                logger.warn("Dimensiones de imagen exceden límite: {}x{}",
                        image.getWidth(), image.getHeight());
                throw new SecurityException(
                        String.format("Dimensiones de imagen exceden el límite: máximo %dx%d",
                                MAX_IMAGE_DIMENSION, MAX_IMAGE_DIMENSION));
            }

            // Validaciones adicionales para seguridad
            validateImageDimensions(image.getWidth(), image.getHeight());

        } catch (SecurityException e) {
            throw e;
        } catch (IOException e) {
            logger.error("Error leyendo contenido de imagen: {}", e.getMessage());
            throw new SecurityException("Error validando contenido de imagen");
        }
    }

    /**
     * Validaciones adicionales de dimensiones de imagen.
     */
    private void validateImageDimensions(int width, int height) throws SecurityException {
        // Rechazar imágenes muy pequeñas (posible garbage)
        if (width < 10 || height < 10) {
            throw new SecurityException("Dimensiones de imagen demasiado pequeñas");
        }

        // Rechazar imágenes con ratio muy extremo (1:100+)
        double ratio = Math.max(width, height) / (double) Math.min(width, height);
        if (ratio > 100) {
            throw new SecurityException("Aspecto de imagen inválido");
        }
    }

    /**
     * Obtiene información sobre los límites permitidos (para mensajes de error).
     */
    public static class ValidationLimits {
        public static final long MAX_IMAGE_SIZE_MB = MAX_IMAGE_SIZE / (1024 * 1024);
        public static final long MAX_SIGNATURE_SIZE_MB = MAX_SIGNATURE_SIZE / (1024 * 1024);
        public static final long MAX_PHOTO_SIZE_MB = MAX_PHOTO_SIZE / (1024 * 1024);
        public static final int MAX_DIMENSION = MAX_IMAGE_DIMENSION;
    }
}
