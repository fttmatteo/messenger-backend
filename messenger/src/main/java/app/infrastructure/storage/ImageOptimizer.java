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

    private static final int MAX_WIDTH = 1200;
    private static final int MAX_HEIGHT = 1200;
    private static final float OUTPUT_QUALITY = 0.75f; // Calidad 0.75 en WebP es superior a 0.9 en JPEG

    /**
     * Optimiza una imagen recibida como InputStream.
     * Si la imagen es más grande que los límites, la redimensiona manteniendo el aspecto.
     * Genera un archivo WebP para máxima velocidad de carga y ahorro de ancho de banda.
     */
    public InputStream optimize(InputStream inputStream, String extension) throws IOException {
        String format = extension.toLowerCase().replace(".", "");

        // No optimizar GIFs
        if ("gif".equals(format)) {
            return inputStream;
        }

        try {
            java.awt.image.BufferedImage resizedImage = Thumbnails.of(inputStream)
                    .size(MAX_WIDTH, MAX_HEIGHT)
                    .asBufferedImage();

            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                javax.imageio.ImageWriter writer = javax.imageio.ImageIO.getImageWritersByFormatName("webp").next();
                javax.imageio.ImageWriteParam param = writer.getDefaultWriteParam();
                
                if (param.canWriteCompressed()) {
                    param.setCompressionMode(javax.imageio.ImageWriteParam.MODE_EXPLICIT);
                    param.setCompressionQuality(OUTPUT_QUALITY);
                }

                try (javax.imageio.stream.ImageOutputStream ios = javax.imageio.ImageIO.createImageOutputStream(outputStream)) {
                    writer.setOutput(ios);
                    writer.write(null, new javax.imageio.IIOImage(resizedImage, null, null), param);
                    writer.dispose();
                }

                byte[] optimizedBytes = outputStream.toByteArray();

                return new ByteArrayInputStream(optimizedBytes);
            }
        } catch (Exception e) {
            log.error("Error optimizando imagen a WebP: {}. Se usará el flujo original si es posible.", e.getMessage());
            throw new IOException("Fallo en la optimización WebP", e);
        }
    }
}
