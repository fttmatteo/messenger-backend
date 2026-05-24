package app.adapter.out.storage;

import app.domain.ports.StoragePort;
import app.infrastructure.storage.ImageOptimizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Adapter de almacenamiento local para desarrollo.
 */
@Component
@ConditionalOnProperty(name = "app.storage.type", havingValue = "local", matchIfMissing = true)
public class LocalStorageAdapter implements StoragePort {

    private final Path storagePath;
    private final ImageOptimizer imageOptimizer;

    public LocalStorageAdapter(
            @Value("${app.storage.path:./uploads}") String storagePath,
            ImageOptimizer imageOptimizer) throws IOException {
        this.storagePath = Paths.get(storagePath).toAbsolutePath();
        this.imageOptimizer = imageOptimizer;
        Files.createDirectories(this.storagePath);
    }

    /**
     * Guarda un archivo en el sistema de archivos local.
     */
    @Override
    public String save(File file, String subDirectory, String customFileName) throws IOException {
        Path subDirPath = storagePath.resolve(subDirectory);
        Files.createDirectories(subDirPath);
        String extension = getExtension(file.getName());
        String format = extension.toLowerCase().replace(".", "");
        boolean isOptimizable = "jpg".equals(format) || "jpeg".equals(format) || "png".equals(format) || "webp".equals(format);

        String finalExtension = isOptimizable ? ".webp" : extension;
        String fileName = customFileName + finalExtension;
        Path targetPath = subDirPath.resolve(fileName);

        if (isOptimizable) {
            boolean isSignature = "signatures".equalsIgnoreCase(subDirectory);
            try (InputStream originalStream = Files.newInputStream(file.toPath());
                    InputStream optimizedStream = imageOptimizer.optimize(originalStream, extension, isSignature)) {
                Files.copy(optimizedStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception e) {
                String fallbackFileName = customFileName + extension;
                Files.copy(file.toPath(), subDirPath.resolve(fallbackFileName), StandardCopyOption.REPLACE_EXISTING);
                return subDirectory + "/" + fallbackFileName;
            }
        } else {
            Files.copy(file.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        }

        String relativePath = subDirectory + "/" + fileName;
        return relativePath;
    }

    /**
     * Recupera un archivo del almacenamiento local.
     */
    @Override
    public InputStream get(String path) {
        Path filePath = storagePath.resolve(path);
        try {
            return Files.exists(filePath) ? Files.newInputStream(filePath) : null;
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public String getUrl(String path) {
        return path;
    }

    /**
     * Elimina un archivo del almacenamiento local.
     */
    public boolean delete(String path) {
        try {
            Path filePath = storagePath.resolve(path);
            boolean deleted = Files.deleteIfExists(filePath);
            return deleted;
        } catch (IOException e) {
            return false;
        }
    }

    private String getExtension(String fileName) {
        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            return fileName.substring(i);
        }
        return "";
    }

    public Path getStoragePath() {
        return storagePath;
    }
}
