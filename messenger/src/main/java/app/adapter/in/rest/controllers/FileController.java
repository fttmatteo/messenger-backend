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
 * 
 * Seguridad:
 * - Protección contra Path Traversal (../../../ attacks)
 * - Validación de nombres de archivo
 * - Verificación de que archivos estén dentro del directorio permitido
 */
@RestController
@RequestMapping("/files")
public class FileController {

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    @Value("${app.storage.path:uploads}")
    private String storageLocation;

    private final String[] subDirectories = { "detections", "signatures", "evidence" };

    /**
     * Recupera un archivo almacenado por su nombre.
     *
     * Busca el archivo en los subdirectorios configurados (detections, signatures,
     * evidence) y lo devuelve como un recurso descargable o visualizable.
     * 
     * Seguridad: Este método incluye protección contra ataques de Path Traversal.
     * Valida que el nombre de archivo no contenga caracteres maliciosos y que
     * el archivo resuelto esté dentro del directorio de almacenamiento permitido.
     *
     * @param filename Nombre del archivo a recuperar.
     * @return ResponseEntity con el recurso del archivo y su tipo de contenido.
     * @throws app.application.exceptions.ResourceNotFoundException si el archivo no
     *                                                              se encuentra.
     * @throws SecurityException                                    si se detecta un
     *                                                              intento de Path
     *                                                              Traversal.
     * @throws Exception                                            si hay un error
     *                                                              al leer el
     *                                                              archivo.
     */
    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> getFile(@PathVariable String filename) throws Exception {
        logger.debug("Solicitando archivo: {}", filename);

        // ========== PROTECCIÓN PATH TRAVERSAL ==========
        // Validar que el nombre de archivo no contenga secuencias peligrosas
        if (filename == null || filename.isEmpty()) {
            logger.warn("Intento de acceso con filename vacío");
            throw new app.application.exceptions.InputsException("Nombre de archivo requerido");
        }

        // Detectar caracteres/secuencias de path traversal
        if (filename.contains("..") ||
                filename.contains("/") ||
                filename.contains("\\") ||
                filename.contains("%2e") || // URL encoded .
                filename.contains("%2f") || // URL encoded /
                filename.contains("%5c")) { // URL encoded \
            logger.error("SEGURIDAD: Intento de Path Traversal detectado. Filename: {}", filename);
            throw new SecurityException("Nombre de archivo inválido");
        }

        // Obtener ruta raíz absoluta y normalizada
        Path rootLocation = Paths.get(storageLocation).toAbsolutePath().normalize();

        for (String subDir : subDirectories) {
            Path file = rootLocation.resolve(subDir).resolve(filename).normalize();

            // ========== VALIDACIÓN CRÍTICA ==========
            // Verificar que el archivo resuelto esté DENTRO del directorio permitido
            if (!file.startsWith(rootLocation)) {
                logger.error("SEGURIDAD: Path Traversal bloqueado. Intento de acceder fuera de: {}", rootLocation);
                throw new SecurityException("Acceso denegado");
            }

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
