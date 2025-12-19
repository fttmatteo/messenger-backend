package app.adapter.out.storage;

import app.domain.ports.StoragePort;
import com.google.auth.ServiceAccountSigner;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.iam.credentials.v1.IamCredentialsClient;
import com.google.cloud.iam.credentials.v1.SignBlobRequest;
import com.google.cloud.iam.credentials.v1.SignBlobResponse;
import com.google.cloud.storage.*;
import com.google.protobuf.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

/**
 * Adaptador de salida para almacenamiento de archivos en Google Cloud Storage.
 *
 * Implementa firma remota vía IAM cuando se ejecuta en Cloud Run (sin clave
 * privada).
 */
@Component
@ConditionalOnProperty(name = "app.storage.type", havingValue = "gcs")
public class GoogleCloudStorageAdapter implements StoragePort {

    private static final Logger logger = LoggerFactory.getLogger(GoogleCloudStorageAdapter.class);

    private final Storage storage;
    private final String bucketName;
    private final int defaultUrlExpirationHours;
    private final GoogleCredentials credentials;

    public GoogleCloudStorageAdapter(
            @Value("${google.cloud.storage.bucket-name}") String bucketName,
            @Value("${google.cloud.storage.project-id}") String projectId,
            @Value("${google.cloud.storage.signed-url-expiration-hours:24}") int urlExpirationHours)
            throws IOException {

        this.bucketName = bucketName;
        this.defaultUrlExpirationHours = urlExpirationHours;

        // Usa Application Default Credentials (ADC)
        this.credentials = GoogleCredentials.getApplicationDefault();

        // Inicializar cliente de Storage
        this.storage = StorageOptions.newBuilder()
                .setProjectId(projectId)
                .setCredentials(credentials)
                .build()
                .getService();

        logger.info("GoogleCloudStorageAdapter inicializado - Bucket: {}, URL expiration: {}h", bucketName,
                urlExpirationHours);
    }

    @Override
    public String save(File file, String subDirectory, String customFileName) throws IOException {
        String originalName = file.getName();
        String extension = getExtension(originalName);
        String fileName = customFileName + extension;

        return uploadToGCS(file, subDirectory, fileName);
    }

    private String uploadToGCS(File file, String subDirectory, String fileName) throws IOException {
        String objectName = subDirectory + "/" + fileName;

        logger.debug("Subiendo archivo a GCS: {}", objectName);

        String contentType = Files.probeContentType(file.toPath());
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        BlobId blobId = BlobId.of(bucketName, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(contentType)
                .build();

        byte[] fileBytes = Files.readAllBytes(file.toPath());
        storage.create(blobInfo, fileBytes);

        logger.info("Archivo subido exitosamente a GCS: {} ({})", objectName, contentType);

        return objectName;
    }

    public String regenerateSignedUrl(String objectName, int expirationHours) {
        return regenerateSignedUrl(objectName, expirationHours, this.credentials);
    }

    private String regenerateSignedUrl(String objectName, int expirationHours, GoogleCredentials creds) {
        logger.debug("Generando URL firmada para: {} (expira en {}h)", objectName, expirationHours);

        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, objectName).build();

        Storage.SignUrlOption signUrlOption;

        // Lógica de detección: ¿Tenemos clave privada local?
        // ServiceAccountCredentials tiene clave privada (environment="local" con json
        // key)
        if (creds instanceof ServiceAccountCredentials) {
            signUrlOption = Storage.SignUrlOption.signWith((ServiceAccountSigner) creds);
        } else {
            // ComputeEngineCredentials (Cloud Run, GKE, GCE) SOLO tiene token, no clave
            // privada.
            // Usamos la API de IAM Credentials para firmar remotamente con la identidad del
            // servicio.
            String serviceAccountEmail = getServiceAccountEmail(creds);

            if (serviceAccountEmail != null) {
                signUrlOption = Storage.SignUrlOption.signWith(new CloudRunServiceAccountSigner(serviceAccountEmail));
            } else {
                logger.warn(
                        "No se pudo determinar el email de la cuenta de servicio. Intentando firma por defecto (podría fallar en Cloud Run).");
                signUrlOption = Storage.SignUrlOption.withV4Signature();
            }
        }

        URL signedUrl = storage.signUrl(
                blobInfo,
                expirationHours,
                TimeUnit.HOURS,
                Storage.SignUrlOption.withV4Signature(),
                signUrlOption);

        return signedUrl.toString();
    }

    private String getServiceAccountEmail(GoogleCredentials creds) {
        logger.info("Iniciando detección de email de cuenta de servicio...");

        // 1. Intentar variable de entorno explícita (Escape hatch)
        String envEmail = System.getenv("GOOGLE_SERVICE_ACCOUNT_EMAIL");
        if (envEmail != null && !envEmail.isBlank()) {
            logger.info("Detectado email vía Variable de Entorno: {}", envEmail);
            return envEmail;
        } else {
            logger.debug("Variable GOOGLE_SERVICE_ACCOUNT_EMAIL no está definida o está vacía.");
        }

        // 2. Intentar obtener de credenciales
        if (creds instanceof com.google.auth.oauth2.ComputeEngineCredentials) {
            String email = ((com.google.auth.oauth2.ComputeEngineCredentials) creds).getAccount();
            if (email != null && !email.isBlank()) {
                logger.info("Detectado email vía Credenciales (ComputeEngineCredentials): {}", email);
                return email;
            }
        }
        logger.debug("No se pudo obtener email de las credenciales actuales. Tipo: {}", creds.getClass().getName());

        // 3. Metadata Server (Fuente de verdad en Cloud Run/GCE)
        try {
            logger.debug("Intentando consultar Metadata Server...");
            String metadataEmail = fetchEmailFromMetadataServer();
            if (metadataEmail != null && !metadataEmail.isBlank()) {
                logger.info("Detectado email vía Metadata Server: {}", metadataEmail);
                return metadataEmail;
            }
        } catch (Exception e) {
            logger.warn("No se pudo obtener email del Metadata Server: {}", e.getMessage());
        }

        logger.error("FALLO TOTAL: No se pudo determinar el email de la cuenta de servicio por ningún método.");
        return null;
    }

    private String fetchEmailFromMetadataServer() throws IOException {
        // This method attempts to fetch the service account email from the Google Cloud
        // Metadata Server.
        // This is typically available in environments like Cloud Run, GCE, GKE.
        // It's a placeholder implementation; a robust solution might use a dedicated
        // library or more error handling.
        String metadataUrl = "http://metadata.google.internal/computeMetadata/v1/instance/service-accounts/default/email";
        URL url = java.net.URI.create(metadataUrl).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Metadata-Flavor", "Google"); // Required for metadata server requests
        connection.setConnectTimeout(2000); // 2 seconds
        connection.setReadTimeout(2000); // 2 seconds

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String inputLine;
                StringBuilder content = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                return content.toString().trim();
            }
        } else {
            logger.debug("Metadata server responded with status code: {}", responseCode);
            // Read error stream for more details if needed
            try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getErrorStream()))) {
                String inputLine;
                StringBuilder errorContent = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    errorContent.append(inputLine);
                }
                logger.debug("Metadata server error response: {}", errorContent.toString());
            } catch (Exception e) {
                logger.debug("Could not read error stream from metadata server: {}", e.getMessage());
            }
            throw new IOException("Failed to fetch email from metadata server. HTTP error code: " + responseCode);
        }
    }

    public String regenerateSignedUrl(String objectName) {
        return regenerateSignedUrl(objectName, defaultUrlExpirationHours);
    }

    @Override
    public File get(String path) {
        throw new UnsupportedOperationException(
                "Para GCS con URLs firmadas, usa directamente la URL almacenada en la base de datos.");
    }

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

    public String extractObjectNameFromSignedUrl(String signedUrl) {
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

    private String getExtension(String fileName) {
        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            return fileName.substring(i);
        }
        return "";
    }

    /**
     * Signer personalizado que usa IAM Credentials API.
     * Necesario para Cloud Run donde no hay clave privada local.
     */
    private static class CloudRunServiceAccountSigner implements ServiceAccountSigner {
        private final String serviceAccountEmail;

        public CloudRunServiceAccountSigner(String serviceAccountEmail) {
            this.serviceAccountEmail = serviceAccountEmail;
        }

        @Override
        public String getAccount() {
            return serviceAccountEmail;
        }

        @Override
        public byte[] sign(byte[] toSign) {
            try (IamCredentialsClient client = IamCredentialsClient.create()) {
                SignBlobRequest request = SignBlobRequest.newBuilder()
                        .setName("projects/-/serviceAccounts/" + serviceAccountEmail)
                        .setPayload(ByteString.copyFrom(toSign))
                        .build();

                SignBlobResponse response = client.signBlob(request);
                return response.getSignedBlob().toByteArray();
            } catch (IOException e) {
                // Envolvemos en RuntimeException porque la interfaz no permite checked
                // exceptions
                throw new RuntimeException("Error firmando blob vía IAM API para " + serviceAccountEmail, e);
            }
        }
    }
}
