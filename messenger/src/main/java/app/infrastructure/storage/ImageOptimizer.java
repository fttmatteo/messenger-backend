package app.infrastructure.storage;

import net.coobird.thumbnailator.Thumbnails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Utilidad profesional para la optimizacion de imagenes antes de su
 * almacenamiento.
 * Implementa redimensionamiento dinamico y compresion de calidad para ahorrar
 * espacio y mejorar el rendimiento de carga en el cliente.
 */
@Component
public class ImageOptimizer {

    private static final Logger log = LoggerFactory.getLogger(ImageOptimizer.class);
    
    private static final int MAX_WIDTH = 1280;
    private static final int MAX_HEIGHT = 1280;
    private static final float PHOTO_QUALITY = 0.85f;
    private static final float SIGNATURE_QUALITY = 0.95f;

    /**
     * Optimiza una imagen convirtiendola a WebP.
     * Si la imagen es mas grande que los limites, la redimensiona manteniendo el aspecto.
     * Elimina metadatos EXIF automaticamente al re-codificar.
     */
    public InputStream optimize(InputStream inputStream, String extension, boolean isSignature) throws IOException {
        String format = extension.toLowerCase().replace(".", "");
        
        if ("webp".equals(format) || "avif".equals(format)) {
            return inputStream;
        }

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            float quality = isSignature ? SIGNATURE_QUALITY : PHOTO_QUALITY;
            
            Thumbnails.of(inputStream)
                    .size(MAX_WIDTH, MAX_HEIGHT)
                    .outputQuality(quality)
                    .outputFormat("webp")
                    .toOutputStream(outputStream);

            byte[] optimizedBytes = outputStream.toByteArray();

            return new ByteArrayInputStream(optimizedBytes);
        } catch (Exception e) {
            log.error("Error optimizando imagen a WebP: {}", e.getMessage());
            throw e;
        }
    }
}
