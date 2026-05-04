package app.domain.services;

import app.domain.model.Photo;
import app.domain.model.Signature;
import app.domain.ports.ServiceDeliveryPort;
import app.domain.ports.StoragePort;
import app.infrastructure.storage.ImageOptimizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Servicio de utilidad para migrar imágenes existentes al formato optimizado WebP.
 * Ayuda a mejorar la velocidad de carga de datos históricos.
 */
@Service
public class ImageMigrationService {

    private static final Logger log = LoggerFactory.getLogger(ImageMigrationService.class);

    @Autowired
    private ServiceDeliveryPort serviceDeliveryPort;

    @Autowired
    private StoragePort storagePort;

    @Autowired
    private ImageOptimizer imageOptimizer;

    /**
     * Migra todas las fotos y firmas del sistema al formato WebP.
     * Retorna el número de archivos procesados exitosamente.
     */
    @Transactional
    public MigrationResult migrateAllToWebP() {
        log.info("Iniciando migración masiva de imágenes a WebP...");
        
        AtomicInteger photosMigrated = new AtomicInteger(0);
        AtomicInteger signaturesMigrated = new AtomicInteger(0);
        AtomicInteger errors = new AtomicInteger(0);

        try {
            List<Photo> allPhotos = serviceDeliveryPort.findAllPhotos();
            for (Photo photo : allPhotos) {
                if (shouldMigrate(photo.getPhotoPath())) {
                    if (processPhotoMigration(photo)) {
                        photosMigrated.incrementAndGet();
                    } else {
                        errors.incrementAndGet();
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error recuperando fotos para migración: {}", e.getMessage());
        }

        try {
            List<Signature> allSignatures = serviceDeliveryPort.findAllSignatures();
            for (Signature sig : allSignatures) {
                if (shouldMigrate(sig.getSignaturePath())) {
                    if (processSignatureMigration(sig)) {
                        signaturesMigrated.incrementAndGet();
                    } else {
                        errors.incrementAndGet();
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error recuperando firmas para migración: {}", e.getMessage());
        }

        log.info("Migración completada. Fotos: {}, Firmas: {}, Errores: {}", 
            photosMigrated.get(), signaturesMigrated.get(), errors.get());
            
        return new MigrationResult(photosMigrated.get(), signaturesMigrated.get(), errors.get());
    }

    private boolean shouldMigrate(String path) {
        if (path == null) return false;
        String lowerPath = path.toLowerCase();
        return lowerPath.endsWith(".jpg") || lowerPath.endsWith(".jpeg") || lowerPath.endsWith(".png");
    }

    private boolean processPhotoMigration(Photo photo) {
        String oldPath = photo.getPhotoPath();
        File oldFile = null;
        try {
            oldFile = storagePort.get(oldPath);
            if (oldFile == null || !oldFile.exists()) return false;

            String newPath = convertAndSave(oldFile, "evidence", "migrated_" + System.currentTimeMillis());
            if (newPath != null) {
                photo.setPhotoPath(newPath);
                serviceDeliveryPort.updatePhoto(photo);
                // Intento de borrado opcional del antiguo en GCS/Local
                try { storagePort.delete(oldPath); } catch (Exception ignored) {}
                return true;
            }
        } catch (Exception e) {
            log.error("Fallo al migrar foto {}: {}. Causa: {}", oldPath, e.getMessage(), e.getCause() != null ? e.getCause().getMessage() : "Desconocida");
        } finally {
            if (oldFile != null && oldFile.exists()) oldFile.delete();
        }
        return false;
    }

    private boolean processSignatureMigration(Signature sig) {
        String oldPath = sig.getSignaturePath();
        File oldFile = null;
        try {
            oldFile = storagePort.get(oldPath);
            if (oldFile == null || !oldFile.exists()) return false;

            String newPath = convertAndSave(oldFile, "signatures", "migrated_sig_" + System.currentTimeMillis());
            if (newPath != null) {
                sig.setSignaturePath(newPath);
                serviceDeliveryPort.updateSignature(sig);
                try { storagePort.delete(oldPath); } catch (Exception ignored) {}
                return true;
            }
        } catch (Exception e) {
            log.warn("Fallo al migrar firma {}: {}", oldPath, e.getMessage());
        } finally {
            if (oldFile != null && oldFile.exists()) oldFile.delete();
        }
        return false;
    }

    private String convertAndSave(File file, String folder, String name) {
        File tempWebp = null;
        try (InputStream is = new FileInputStream(file);
             InputStream optimized = imageOptimizer.optimize(is, file.getName())) {
            
            tempWebp = File.createTempFile("migrate_webp_", ".webp");
            java.nio.file.Files.copy(optimized, tempWebp.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            
            return storagePort.save(tempWebp, folder, name);
        } catch (Exception e) {
            log.error("Error en conversión durante migración: {}", e.getMessage());
            return null;
        } finally {
            if (tempWebp != null && tempWebp.exists()) tempWebp.delete();
        }
    }

    public record MigrationResult(int photos, int signatures, int errors) {}
}
