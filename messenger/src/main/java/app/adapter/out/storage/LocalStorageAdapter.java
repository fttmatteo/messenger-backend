package app.adapter.out.storage;

import app.domain.ports.StoragePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * Adaptador de almacenamiento local para desarrollo y tests.
 * 
 * Este adaptador implementa StoragePort guardando archivos en el sistema
 * de archivos local en lugar de Google Cloud Storage.
 * 
 * Se activa cuando app.storage.type=local
 * 
 * Uso recomendado:
 * - Desarrollo local sin credenciales de GCS
 * - Tests automatizados
 * - Debugging
 * 
 * @see app.domain.ports.StoragePort
 */
@Component
@ConditionalOnProperty(name = "app.storage.type", havingValue = "local", matchIfMissing = true)
public class LocalStorageAdapter implements StoragePort {

    private static final Logger logger = LoggerFactory.getLogger(LocalStorageAdapter.class);

    private final Path storagePath;

    public LocalStorageAdapter(
            @Value("${app.storage.path:./uploads}") String storagePath) throws IOException {
        this.storagePath = Paths.get(storagePath).toAbsolutePath();

        // Crear directorio si no existe
        Files.createDirectories(this.storagePath);

        logger.info("LocalStorageAdapter inicializado - Path: {}", this.storagePath);
    }

    /**
     * Guarda un archivo en el sistema de archivos local.
     * 
     * @param file           Archivo a guardar
     * @param subDirectory   Subdirectorio dentro del path base
     * @param customFileName Nombre personalizado (sin extensión)
     * @return Path relativo del archivo guardado
     * @throws IOException si hay error al guardar
     */
    @Override
    public String save(File file, String subDirectory, String customFileName) throws IOException {
        // Crear subdirectorio si no existe
        Path subDirPath = storagePath.resolve(subDirectory);
        Files.createDirectories(subDirPath);

        // Generar nombre de archivo
        String extension = getExtension(file.getName());
        String fileName = customFileName + extension;
        Path targetPath = subDirPath.resolve(fileName);

        // Copiar archivo
        Files.copy(file.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        String relativePath = subDirectory + "/" + fileName;
        logger.debug("Archivo guardado localmente: {}", relativePath);

        return relativePath;
    }

    /**
     * Obtiene un archivo del sistema de archivos local.
     * 
     * @param path Path relativo del archivo
     * @return File si existe, null si no
     */
    @Override
    public File get(String path) {
        Path filePath = storagePath.resolve(path);
        File file = filePath.toFile();

        if (file.exists()) {
            return file;
        }

        logger.warn("Archivo no encontrado: {}", path);
        return null;
    }

    /**
     * Elimina un archivo del sistema de archivos local.
     * 
     * @param path Path relativo del archivo
     * @return true si se eliminó, false si no existía
     */
    public boolean delete(String path) {
        try {
            Path filePath = storagePath.resolve(path);
            boolean deleted = Files.deleteIfExists(filePath);

            if (deleted) {
                logger.debug("Archivo eliminado: {}", path);
            }

            return deleted;
        } catch (IOException e) {
            logger.error("Error al eliminar archivo: {}", path, e);
            return false;
        }
    }

    /**
     * Extrae la extensión de un nombre de archivo.
     */
    private String getExtension(String fileName) {
        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            return fileName.substring(i);
        }
        return "";
    }

    /**
     * Obtiene el path absoluto base de almacenamiento.
     */
    public Path getStoragePath() {
        return storagePath;
    }
}
