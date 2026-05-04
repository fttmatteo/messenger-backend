package app.adapter.in.rest.controllers;

import app.domain.services.ImageMigrationService;
import app.domain.services.ImageMigrationService.MigrationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controlador para utilidades administrativas y mantenimiento del sistema.
 */
@RestController
@RequestMapping("/admin/utilities")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUtilityController {

    @Autowired
    private ImageMigrationService migrationService;

    /**
     * Inicia la migración de todas las imágenes existentes al formato optimizado WebP.
     * Este proceso mejora la velocidad de carga histórica.
     */
    @PostMapping("/migrate-images-to-webp")
    public ResponseEntity<Map<String, Object>> migrateImages() {
        MigrationResult result = migrationService.migrateAllToWebP();
        
        return ResponseEntity.ok(Map.of(
            "message", "Proceso de migración completado",
            "photosMigrated", result.photos(),
            "signaturesMigrated", result.signatures(),
            "errors", result.errors()
        ));
    }
}
