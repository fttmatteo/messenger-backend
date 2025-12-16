package app.adapter.out.storage;

import app.domain.ports.StoragePort;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

/**
 * Adaptador de salida para almacenamiento de archivos en Google Cloud Storage.
 * 
 * Este adaptador implementa StoragePort y proporciona almacenamiento seguro en
 * la nube
 * para evidencias fotográficas, firmas digitales y otros archivos del sistema,
 * utilizando URLs firmadas temporales para máxima seguridad.
 * 
 * Características principales:
 * - Almacenamiento privado con URLs firmadas temporales
 * - Generación automática de nombres únicos (UUID)
 * - Detección automática de content-type
 * - Soporte para subdirectorios organizados
 * - URLs con expiración configurable
 * 
 * Ventajas de URLs firmadas:
 * - Acceso temporal controlado (URLs expiran automáticamente)
 * - No requiere bucket público (mayor seguridad)
 * - Protección contra acceso no autorizado
 * - Ideal para evidencias legales y datos sensibles
 * 
 * Ventajas sobre almacenamiento local:
 * - Escalabilidad infinita sin límites de disco
 * - CDN global para entrega rápida desde cualquier ubicación
 * - Backups automáticos y redundancia
 * - No consume espacio del servidor de aplicación
 * - Alta disponibilidad (99.95% SLA)
 * 
 * Configuración requerida:
 * - google.cloud.storage.bucket-name: Nombre del bucket de GCS
 * - google.cloud.storage.project-id: ID del proyecto de Google Cloud
 * - google.cloud.storage.signed-url-expiration-hours: Duración de URLs
 * (default: 24h)
 * 
 * Autenticación:
 * - Usa Application Default Credentials (ADC)
 * - Local: Variable GOOGLE_APPLICATION_CREDENTIALS
 * - Producción: Service Account del entorno
 * 
 * @see app.domain.ports.StoragePort
 * @see com.google.cloud.storage.Storage
 */
@Component
public class GoogleCloudStorageAdapter implements StoragePort {

    private static final Logger logger = LoggerFactory.getLogger(GoogleCloudStorageAdapter.class);

    private final Storage storage;
    private final String bucketName;
    private final int defaultUrlExpirationHours;

    public GoogleCloudStorageAdapter(
            @Value("${google.cloud.storage.bucket-name}") String bucketName,
            @Value("${google.cloud.storage.project-id}") String projectId,
            @Value("${google.cloud.storage.signed-url-expiration-hours:24}") int urlExpirationHours)
            throws IOException {

        this.bucketName = bucketName;
        this.defaultUrlExpirationHours = urlExpirationHours;

        // Usa Application Default Credentials (ADC)
        GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();

        // Inicializar cliente de Storage
        this.storage = StorageOptions.newBuilder()
                .setProjectId(projectId)
                .setCredentials(credentials)
                .build()
                .getService();

        logger.info("GoogleCloudStorageAdapter inicializado - Bucket: {}, URL expiration: {}h", bucketName,
                urlExpirationHours);
    }

    /**
     * Guarda un archivo en Google Cloud Storage con nombre personalizado.
     * 
     * Usa el nombre proporcionado (añadiendo la extensión del archivo original)
     * y sube el archivo al subdirectorio especificado.
     * 
     * @param file           Archivo a guardar
     * @param subDirectory   Subdirectorio en el bucket
     * @param customFileName Nombre personalizado (sin extensión)
     * @return Path del objeto en GCS (ej: "signatures/custom-name.png")
     * @throws IOException si hay error al subir el archivo
     */
    @Override
    public String save(File file, String subDirectory, String customFileName) throws IOException {
        String originalName = file.getName();
        String extension = getExtension(originalName);
        String fileName = customFileName + extension;

        return uploadToGCS(file, subDirectory, fileName);
    }

    /**
     * Sube un archivo a Google Cloud Storage y retorna la ruta del objeto (Object
     * Name).
     * 
     * El archivo se almacena de forma PRIVADA en el bucket.
     * Retorna el path relativo (ej: "photos/uuid.jpg") que se debe guardar en BD.
     * La URL firmada se generará posteriormente bajo demanda usando
     * regenerateSignedUrl.
     * 
     * @param file         Archivo a subir
     * @param subDirectory Subdirectorio en el bucket (ej: "photos", "signatures")
     * @param fileName     Nombre del archivo con extensión
     * @return Ruta del objeto en el bucket (Object Name)
     * @throws IOException Si ocurre un error al subir
     */
    private String uploadToGCS(File file, String subDirectory, String fileName) throws IOException {
        // Construir el path completo en GCS
        String objectName = subDirectory + "/" + fileName;

        logger.debug("Subiendo archivo a GCS: {}", objectName);

        // Detectar content type basado en extensión
        String contentType = Files.probeContentType(file.toPath());
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        // Configurar metadata del objeto (PRIVADO por defecto)
        BlobId blobId = BlobId.of(bucketName, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(contentType)
                .build();

        // Subir archivo
        byte[] fileBytes = Files.readAllBytes(file.toPath());
        storage.create(blobInfo, fileBytes);

        logger.info("Archivo subido exitosamente a GCS: {} ({})", objectName, contentType);

        // Retornamos el PATH relativo (ej: photos/abc.jpg)
        // La URL firmada se generará solo al consultar (GET)
        return objectName;
    }

    /**
     * Genera una URL firmada para acceso temporal a un objeto en GCS.
     * 
     * La URL firmada permite acceso sin autenticación durante el tiempo
     * especificado.
     * Después de la expiración, la URL deja de funcionar automáticamente.
     * 
     * @param blob            Objeto de GCS para el cual generar la URL
     * @param expirationHours Horas hasta que expire la URL
     * @return URL firmada temporal
     */
    private String generateSignedUrl(Blob blob, int expirationHours) {
        URL signedUrl = blob.signUrl(
                expirationHours,
                TimeUnit.HOURS,
                Storage.SignUrlOption.withV4Signature());

        return signedUrl.toString();
    }

    /**
     * Genera una nueva URL firmada para un objeto existente.
     * 
     * Útil cuando una URL firmada ha expirado y necesitas regenerarla
     * sin volver a subir el archivo.
     * 
     * @param objectName      Nombre del objeto en GCS (ej: "photos/uuid.jpg")
     * @param expirationHours Horas hasta que expire la nueva URL
     * @return Nueva URL firmada temporal
     */
    public String regenerateSignedUrl(String objectName, int expirationHours) {
        logger.debug("Generando URL firmada para: {} (expira en {}h)", objectName, expirationHours);

        BlobId blobId = BlobId.of(bucketName, objectName);
        Blob blob = storage.get(blobId);

        if (blob == null) {
            logger.error("Objeto no encontrado en GCS: {}", objectName);
            throw new IllegalArgumentException("Objeto no encontrado: " + objectName);
        }

        return generateSignedUrl(blob, expirationHours);
    }

    /**
     * Genera una nueva URL firmada con duración por defecto.
     * 
     * @param objectName Nombre del objeto en GCS
     * @return Nueva URL firmada temporal
     */
    public String regenerateSignedUrl(String objectName) {
        return regenerateSignedUrl(objectName, defaultUrlExpirationHours);
    }

    @Override
    public File get(String path) {
        // Para URLs firmadas, el path almacenado en BD es la URL firmada completa
        // Si necesitas descargar el archivo, extrae el objectName de la URL

        throw new UnsupportedOperationException(
                "Para GCS con URLs firmadas, usa directamente la URL almacenada en la base de datos. " +
                        "Si la URL expiró, usa regenerateSignedUrl(objectName)");
    }

    /**
     * Elimina un archivo de Google Cloud Storage.
     * 
     * @param objectName Nombre del objeto en GCS (ej: "photos/uuid.jpg")
     * @return true si se eliminó exitosamente
     */
    public boolean delete(String objectName) {
        logger.debug("Eliminando archivo de GCS: {}", objectName);
        BlobId blobId = BlobId.of(bucketName, objectName);
        boolean deleted = storage.delete(blobId);
        if (deleted) {
            logger.info("Archivo eliminado de GCS: {}", objectName);
        } else {
            logger.warn("No se pudo eliminar el archivo de GCS (no existe): {}", objectName);
        }
        return deleted;
    }

    /**
     * Extrae el nombre del objeto de una URL firmada.
     * 
     * Útil para obtener el objectName cuando solo tienes la URL firmada.
     * 
     * @param signedUrl URL firmada completa
     * @return Nombre del objeto (path en el bucket)
     */
    public String extractObjectNameFromSignedUrl(String signedUrl) {
        // URL format: https://storage.googleapis.com/bucket-name/object/path?X-Goog-...
        String prefix = "https://storage.googleapis.com/" + bucketName + "/";

        if (!signedUrl.startsWith(prefix)) {
            throw new IllegalArgumentException("URL firmada inválida");
        }

        String pathWithParams = signedUrl.substring(prefix.length());
        int queryStart = pathWithParams.indexOf('?');

        if (queryStart > 0) {
            return pathWithParams.substring(0, queryStart);
        }

        return pathWithParams;
    }

    /**
     * Extrae la extensión de un nombre de archivo.
     * 
     * @param fileName Nombre del archivo
     * @return Extensión con punto (ej: ".jpg") o cadena vacía si no tiene
     */
    private String getExtension(String fileName) {
        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            return fileName.substring(i);
        }
        return "";
    }
}
