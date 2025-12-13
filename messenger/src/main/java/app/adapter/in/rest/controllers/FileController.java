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

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Controlador REST para servir archivos estáticos.
 * 
 * <p>
 * Proporciona endpoints para acceder a archivos almacenados como
 * fotos, firmas y documentos del sistema.
 * </p>
 */
@RestController
@RequestMapping("/api/files")
public class FileController {

    @Value("${app.storage.path:uploads}")
    private String storageLocation;

    private final String[] subDirectories = { "detections", "signatures", "evidence" };

    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> getFile(@PathVariable String filename) {
        try {
            Path rootLocation = Paths.get(storageLocation);

            for (String subDir : subDirectories) {
                Path file = rootLocation.resolve(subDir).resolve(filename);
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

            return ResponseEntity.notFound().build();

        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
