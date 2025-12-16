package app.adapter.in.rest.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Controlador REST para servir archivos estáticos.
 *
 * Proporciona endpoints para acceder a archivos almacenados como
 * fotos, firmas y documentos del sistema de forma pública.
 */
@RestController
@RequestMapping("/api/files")
public class FileController {

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    @Value("${app.storage.path:uploads}")
    private String storageLocation;

    private final String[] subDirectories = { "detections", "signatures", "evidence" };

    /**
     * Recupera un archivo almacenado por su nombre.
     *
     * Busca el archivo en los subdirectorios configurados (detections, signatures,
     * evidence)
     * y lo devuelve como un recurso descargable o visualizable.
     *
     * @param filename Nombre del archivo a recuperar.
     * @return ResponseEntity con el recurso del archivo y su tipo de contenido.
     * @throws app.application.exceptions.ResourceNotFoundException si el archivo no
     *                                                              se encuentra.
     * @throws Exception                                            si hay un error
     *                                                              al leer el
     *                                                              archivo.
     */
    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> getFile(@PathVariable String filename) throws Exception {
        logger.debug("Solicitando archivo: {}", filename);
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
                    logger.info("Archivo entregado: {} ({})", filename, contentType);
                    return ResponseEntity.ok()
                            .contentType(MediaType.parseMediaType(contentType))
                            .body(resource);
                }
            }
        }

        logger.warn("Archivo no encontrado: {}", filename);
        throw new app.application.exceptions.ResourceNotFoundException("Archivo " + filename + " no encontrado");
    }
}
