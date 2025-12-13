package app.adapter.out.files;

import app.domain.ports.StoragePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Adaptador para almacenamiento de archivos en el sistema de archivos local.
 * Implementa StoragePort para guardar y recuperar evidencias y firmas.
 */
@Component
public class FileSystemStorageAdapter implements StoragePort {

    @Value("${app.storage.path:uploads}")
    private String storageLocation;

    @Override
    public String save(File file, String subDirectory) throws IOException {
        String originalName = file.getName();
        String extension = getExtension(originalName);
        String fileName = UUID.randomUUID().toString() + extension;

        return saveWithFileName(file, subDirectory, fileName);
    }

    @Override
    public String save(File file, String subDirectory, String customFileName) throws IOException {
        String originalName = file.getName();
        String extension = getExtension(originalName);
        String fileName = customFileName + extension;

        return saveWithFileName(file, subDirectory, fileName);
    }

    private String saveWithFileName(File file, String subDirectory, String fileName) throws IOException {
        Path rootLocation = Paths.get(storageLocation);
        Path targetDir = rootLocation.resolve(subDirectory);

        if (!Files.exists(targetDir)) {
            Files.createDirectories(targetDir);
        }

        Path targetPath = targetDir.resolve(fileName);
        Files.copy(file.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        return targetPath.toString();
    }

    private String getExtension(String fileName) {
        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            return fileName.substring(i);
        }
        return "";
    }

    @Override
    public File get(String path) {
        return new File(path);
    }
}
