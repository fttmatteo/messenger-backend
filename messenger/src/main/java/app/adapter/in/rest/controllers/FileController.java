package app.adapter.in.rest.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import app.application.exceptions.InputsException;
import app.application.exceptions.ResourceNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controlador REST para servir archivos estáticos (fotos, firmas).
 */
@RestController
@RequestMapping("/files")
public class FileController {

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    @Value("${app.storage.path:uploads}")
    private String storageLocation;

    private final String[] subDirectories = { "detections", "signatures", "evidence" };

    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> getFile(@PathVariable String filename) throws Exception {

        if (filename == null || filename.isEmpty()) {
            throw new InputsException("Nombre de archivo requerido");
        }
        if (filename.contains("..") ||
                filename.contains("/") ||
                filename.contains("\\") ||
                filename.contains("%2e") ||
                filename.contains("%2f") ||
                filename.contains("%5c")) {
            logger.warn("Intento de Path Traversal detectado: {}", filename);
            throw new SecurityException("Nombre de archivo inválido");
        }

        Path rootLocation = Paths.get(storageLocation).toAbsolutePath().normalize();

        for (String subDir : subDirectories) {
            Path file = rootLocation.resolve(subDir).resolve(filename).normalize();

            if (!file.startsWith(rootLocation)) {
                logger.warn("Intento de acceso fuera del directorio raiz: {}", filename);
                throw new SecurityException("Acceso denegado");
            }

            if (Files.exists(file) && Files.isReadable(file)) {
                Resource resource = new UrlResource(file.toUri());
                if (resource.exists() || resource.isReadable()) {
                    String contentType = Files.probeContentType(file);
                    if (contentType == null) {
                        contentType = "application/octet-stream";
                    }
                    return ResponseEntity.ok()
                            .contentType(MediaType.parseMediaType(contentType))
                            .body(resource);
                }
            }
        }
        throw new ResourceNotFoundException("Archivo " + filename + " no encontrado");
    }
}
