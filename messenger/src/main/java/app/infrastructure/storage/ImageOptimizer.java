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
    private static final float OUTPUT_QUALITY = 0.75f;

    /**
     * Optimiza una imagen recibida como InputStream.
     * Si la imagen es más grande que los límites, la redimensiona manteniendo el
     * aspecto.
     * Aplica compresión de calidad JPEG.
     * 
     * @param inputStream Stream de la imagen original
     * @param extension   Extensión del archivo (jpg, png, etc.)
     * @return InputStream de la imagen optimizada
     * @throws IOException Si ocurre un error durante el procesamiento
     */
    public InputStream optimize(InputStream inputStream, String extension) throws IOException {
        String format = extension.toLowerCase().replace(".", "");

        // No optimizar GIFs (animaciones se perderían con redimensionamiento simple)
        if ("gif".equals(format)) {
            return inputStream;
        }

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            log.debug("Iniciando optimización de imagen formato: {}", format);

            Thumbnails.of(inputStream)
                    .size(MAX_WIDTH, MAX_HEIGHT)
                    .outputQuality(OUTPUT_QUALITY)
                    .outputFormat("jpg") // Convertimos a JPG para máxima compresión
                    .toOutputStream(outputStream);

            byte[] optimizedBytes = outputStream.toByteArray();
            log.info("Imagen optimizada. Tamaño final: {} bytes", optimizedBytes.length);

            return new ByteArrayInputStream(optimizedBytes);
        } catch (Exception e) {
            log.error("Error optimizando imagen, se usará el archivo original: {}", e.getMessage());
            // En caso de error, intentamos resetear el stream si es posible o devolver el
            // original
            // Nota: El original puede estar consumido, por lo que lo ideal es que el
            // llamador
            // maneje la re-creación del stream si falla.
            throw e;
        }
    }
}
