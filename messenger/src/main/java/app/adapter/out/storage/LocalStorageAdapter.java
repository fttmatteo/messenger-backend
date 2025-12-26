package app.adapter.out.storage;

import app.domain.ports.StoragePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import java.io.File;
import java.io.IOException;
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

    public LocalStorageAdapter(
            @Value("${app.storage.path:./uploads}") String storagePath) throws IOException {
        this.storagePath = Paths.get(storagePath).toAbsolutePath();
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
        String fileName = customFileName + extension;
        Path targetPath = subDirPath.resolve(fileName);
        Files.copy(file.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        String relativePath = subDirectory + "/" + fileName;
        return relativePath;
    }

    /**
     * Recupera un archivo del almacenamiento local.
     */
    @Override
    public File get(String path) {
        Path filePath = storagePath.resolve(path);
        File file = filePath.toFile();

        if (file.exists()) {
            return file;
        }

        return null;
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
