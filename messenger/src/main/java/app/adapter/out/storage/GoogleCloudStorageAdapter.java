package app.adapter.out.storage;

import app.domain.ports.StoragePort;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Adaptador para almacenamiento de archivos en Google Cloud Storage con URLs
 * firmadas.
 * 
 * Implementa StoragePort para guardar y recuperar evidencias y firmas en la
 * nube
 * utilizando URLs firmadas temporales para máxima seguridad.
 * 
 * Ventajas de URLs firmadas:
 * - Acceso temporal controlado (URLs expiran automáticamente)
 * - No requiere bucket público (mayor seguridad)
 * - Protección contra acceso no autorizado
 * - Ideal para evidencias legales y datos sensibles
 * 
 * Ventajas sobre almacenamiento local:
 * - Escalabilidad infinita
 * - CDN global para entrega rápida
 * - Backups automáticos
 * - No consume espacio del servidor
 */
@Component
public class GoogleCloudStorageAdapter implements StoragePort {

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
    }

    @Override
    public String save(File file, String subDirectory) throws IOException {
        String originalName = file.getName();
        String extension = getExtension(originalName);
        String fileName = UUID.randomUUID().toString() + extension;

        return uploadToGCS(file, subDirectory, fileName);
    }

    @Override
    public String save(File file, String subDirectory, String customFileName) throws IOException {
        String originalName = file.getName();
        String extension = getExtension(originalName);
        String fileName = customFileName + extension;

        return uploadToGCS(file, subDirectory, fileName);
    }

    /**
     * Sube un archivo a Google Cloud Storage y retorna una URL firmada temporal.
     * 
     * El archivo se almacena de forma PRIVADA en el bucket. La URL firmada generada
     * permite acceso temporal sin necesidad de autenticación adicional.
     * 
     * @param file         Archivo a subir
     * @param subDirectory Subdirectorio en el bucket (ej: "photos", "signatures")
     * @param fileName     Nombre del archivo con extensión
     * @return URL firmada temporal para acceder al archivo
     * @throws IOException Si ocurre un error al subir
     */
    private String uploadToGCS(File file, String subDirectory, String fileName) throws IOException {
        // Construir el path completo en GCS
        String objectName = subDirectory + "/" + fileName;

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
        Blob blob = storage.create(blobInfo, fileBytes);

        // Generar y retornar URL firmada
        return generateSignedUrl(blob, defaultUrlExpirationHours);
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
        BlobId blobId = BlobId.of(bucketName, objectName);
        Blob blob = storage.get(blobId);

        if (blob == null) {
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
        BlobId blobId = BlobId.of(bucketName, objectName);
        return storage.delete(blobId);
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
     * Extrae la extensión del nombre de archivo.
     */
    private String getExtension(String fileName) {
        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            return fileName.substring(i);
        }
        return "";
    }
}
