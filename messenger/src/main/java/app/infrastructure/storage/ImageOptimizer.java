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
 * Utilidad profesional para la optimización de imágenes antes de su
 * almacenamiento.
 * Implementa redimensionamiento dinámico y compresión de calidad para ahorrar
 * espacio
 * y mejorar el rendimiento de carga en el cliente.
 */
@Component
public class ImageOptimizer {

    private static final Logger log = LoggerFactory.getLogger(ImageOptimizer.class);
    
    private static final int MAX_WIDTH = 1280;
    private static final int MAX_HEIGHT = 1280;
    private static final float PHOTO_QUALITY = 0.85f;
    private static final float SIGNATURE_QUALITY = 0.95f;

    /**
     * Optimiza una imagen convirtiéndola a WebP.
     * Si la imagen es más grande que los límites, la redimensiona manteniendo el aspecto.
     * Elimina metadatos EXIF automáticamente al re-codificar.
     */
    public InputStream optimize(InputStream inputStream, String extension, boolean isSignature) throws IOException {
        String format = extension.toLowerCase().replace(".", "");
        
        if ("gif".equals(format)) {
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
