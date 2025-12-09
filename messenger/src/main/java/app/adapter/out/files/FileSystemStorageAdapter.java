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

@Component
public class FileSystemStorageAdapter implements StoragePort {

    @Value("${app.storage.path:uploads}")
    private String storageLocation;

    @Override
    public String save(File file, String subDirectory) throws IOException {
        Path rootLocation = Paths.get(storageLocation);
        Path targetDir = rootLocation.resolve(subDirectory);

        if (!Files.exists(targetDir)) {
            Files.createDirectories(targetDir);
        }

        // Generate unique filename to avoid collisions
        String originalName = file.getName();
        String extension = "";
        int i = originalName.lastIndexOf('.');
        if (i > 0) {
            extension = originalName.substring(i);
        }
        String fileName = UUID.randomUUID().toString() + extension;

        Path targetPath = targetDir.resolve(fileName);
        Files.copy(file.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        return targetPath.toString();
    }

    @Override
    public File get(String path) {
        return new File(path);
    }
}
