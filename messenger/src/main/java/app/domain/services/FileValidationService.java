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

    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/avif");

    private static final Set<String> ALLOWED_GIF_TYPES = Set.of(
            "image/webp");

    private static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024;
    private static final long MAX_PHOTO_SIZE = 10 * 1024 * 1024;
    private static final long MAX_SIGNATURE_SIZE = 2 * 1024 * 1024;
    private static final long MAX_GIF_SIZE = 5 * 1024 * 1024;
    private static final int MAX_IMAGE_DIMENSION = 4096;

    /**
     * Valida un archivo de imagen genérico (para creación de servicios).
     * Usado para: imágenes de placas detectadas
     */
    public void validateImageFile(MultipartFile file) throws SecurityException {
        if (file == null) {
            throw new SecurityException("Archivo no proporcionado");
        }

        if (file.isEmpty()) {
            throw new SecurityException("Archivo vacío");
        }

        if (file.getSize() > MAX_IMAGE_SIZE) {
            logger.warn("Archivo de imagen excede tamaño máximo: {} bytes",
                    file.getSize());
            throw new SecurityException(
                    String.format("Archivo demasiado grande. Máximo: %dMB",
                            MAX_IMAGE_SIZE / (1024 * 1024)));
        }

        String detectedType = detectMimeType(file);
        if (!ALLOWED_IMAGE_TYPES.contains(detectedType)) {
            logger.warn("Tipo de archivo no permitido: {}", detectedType);
            throw new SecurityException("Tipo de archivo no permitido: " + detectedType);
        }

        validateImageContent(file);

    }

    /**
     * Valida un archivo de firma (con validaciones más estrictas).
     * Usado para: firmas digitales en entregas
     */
    public void validateSignatureFile(MultipartFile file) throws SecurityException {
        if (file == null) {
            throw new SecurityException("Archivo de firma no proporcionado");
        }

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

        String detectedType = detectMimeType(file);
        if (!ALLOWED_IMAGE_TYPES.contains(detectedType)) {
            logger.warn("Tipo de firma no permitido: {}", detectedType);
            throw new SecurityException("Tipo de archivo de firma no permitido");
        }

        validateImageContent(file);

    }

    /**
     * Valida archivos de fotos (evidencia).
     * Usado para: fotos de entregas/evidencia
     */
    public void validatePhotoFile(MultipartFile file) throws SecurityException {
        if (file == null) {
            throw new SecurityException("Archivo de foto no proporcionado");
        }

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

    }

    /**
     * Valida archivos GIF de captura de firma.
     * Usado para: GIFs de captura durante firma
     */
    public void validateGifFile(MultipartFile file) throws SecurityException {
        if (file == null) {
            throw new SecurityException("Archivo de animación no proporcionado");
        }

        if (file.isEmpty()) {
            throw new SecurityException("Archivo de animación vacío");
        }

        if (file.getSize() > MAX_GIF_SIZE) {
            logger.warn("Archivo de animación excede tamaño máximo: {} bytes",
                    file.getSize());
            throw new SecurityException(
                    String.format("Animación demasiado grande. Máximo: %dMB",
                            MAX_GIF_SIZE / (1024 * 1024)));
        }

        String detectedType = detectMimeType(file);
        if (!ALLOWED_GIF_TYPES.contains(detectedType)) {
            logger.warn("Tipo de animación no permitido: {}", detectedType);
            throw new SecurityException("Tipo de archivo de animación no permitido (Solo WebP Animado o GIF)");
        }
    }

    /**
     * Detecta el tipo MIME real del archivo (no solo la extensión).
     */
    private String detectMimeType(MultipartFile file) throws SecurityException {
        try {
            String contentType = file.getContentType();

            if (contentType == null || contentType.equals("application/octet-stream")) {
                byte[] bytes = new byte[12];
                try (var is = file.getInputStream()) {
                    is.read(bytes);
                }

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

        if (bytes[0] == (byte) 0x89 && bytes[1] == 0x50 &&
                bytes[2] == 0x4E && bytes[3] == 0x47) {
            return "image/png";
        }

        if (bytes[0] == (byte) 0xFF && bytes[1] == (byte) 0xD8 &&
                bytes[2] == (byte) 0xFF) {
            return "image/jpeg";
        }

        if (bytes[0] == 0x52 && bytes[1] == 0x49 &&
                bytes[2] == 0x46 && bytes[3] == 0x46) {
            if (bytes.length >= 12 &&
                    bytes[8] == 0x57 && bytes[9] == 0x45 &&
                    bytes[10] == 0x42 && bytes[11] == 0x50) {
                return "image/webp";
            }
        }

        if (bytes.length >= 12 &&
                bytes[4] == 0x66 && bytes[5] == 0x74 && bytes[6] == 0x79 && bytes[7] == 0x70 &&
                bytes[8] == 0x61 && bytes[9] == 0x76 && bytes[10] == 0x69 && bytes[11] == 0x66) {
            return "image/avif";
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

            if (image.getWidth() > MAX_IMAGE_DIMENSION ||
                    image.getHeight() > MAX_IMAGE_DIMENSION) {
                logger.warn("Dimensiones de imagen exceden límite: {}x{}",
                        image.getWidth(), image.getHeight());
                throw new SecurityException(
                        String.format("Dimensiones de imagen exceden el límite: máximo %dx%d",
                                MAX_IMAGE_DIMENSION, MAX_IMAGE_DIMENSION));
            }

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
        if (width < 10 || height < 10) {
            throw new SecurityException("Dimensiones de imagen demasiado pequeñas");
        }

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
