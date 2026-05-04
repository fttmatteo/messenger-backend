package app.adapter.out.storage;

import app.domain.ports.StorageCachePort;
import app.domain.ports.StoragePort;
import app.infrastructure.storage.ImageOptimizer;
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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Adapter de Google Cloud Storage para almacenamiento de archivos.
 */
@Component
@ConditionalOnProperty(name = "app.storage.type", havingValue = "gcs")
public class GoogleCloudStorageAdapter implements StoragePort {

    private static final Logger logger = LoggerFactory.getLogger(GoogleCloudStorageAdapter.class);

    private final StorageCachePort cachePort;
    private static final int CACHE_EXPIRATION_MARGIN_HOURS = 1;

    private final Storage storage;
    private final String bucketName;
    private final int defaultUrlExpirationHours;
    private final GoogleCredentials credentials;
    private final ImageOptimizer imageOptimizer;

    public GoogleCloudStorageAdapter(
            @Value("${google.cloud.storage.bucket-name}") String bucketName,
            @Value("${google.cloud.storage.signed-url-expiration-hours:24}") int urlExpirationHours,
            ImageOptimizer imageOptimizer,
            StorageCachePort cachePort,
            Storage storage,
            GoogleCredentials credentials)
            throws IOException {

        this.bucketName = bucketName;
        this.defaultUrlExpirationHours = urlExpirationHours;
        this.imageOptimizer = imageOptimizer;
        this.cachePort = cachePort;
        this.storage = storage;
        this.credentials = credentials;
    }

    /**
     * Sube un archivo al bucket de Google Cloud Storage.
     * Retorna el nombre del objeto almacenado.
     */
    @Override
    public String save(File file, String subDirectory, String customFileName) throws IOException {
        String originalName = file.getName();
        String extension = getExtension(originalName);
        String fileName = customFileName + extension;

        return uploadToGCS(file, subDirectory, fileName);
    }

    private String uploadToGCS(File file, String subDirectory, String fileName) throws IOException {
        String objectName = subDirectory + "/" + fileName;
        String extension = getExtension(fileName);
        String format = extension.toLowerCase().replace(".", "");
        boolean shouldOptimize = "jpg".equals(format) || "jpeg".equals(format) || "png".equals(format);

        String contentType = Files.probeContentType(file.toPath());
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        // Optimizar si es imagen (JPEG/PNG)
        if (shouldOptimize) {
            try (InputStream originalStream = Files.newInputStream(file.toPath());
                    InputStream optimizedStream = imageOptimizer.optimize(originalStream, extension)) {

                String webpObjectName = subDirectory + "/" + fileName.substring(0, fileName.lastIndexOf('.')) + ".webp";
                BlobId blobId = BlobId.of(bucketName, webpObjectName);
                
                BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                        .setContentType("image/webp")
                        .setCacheControl("public, max-age=604800")
                        .build();

                storage.createFrom(blobInfo, optimizedStream);
                return webpObjectName;
            } catch (Exception e) {
                logger.warn("Falló optimización a WebP, se sube original: {}", e.getMessage());
            }
        }

        BlobId blobId = BlobId.of(bucketName, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(contentType)
                .setCacheControl("public, max-age=604800")
                .build();

        byte[] fileBytes = Files.readAllBytes(file.toPath());
        storage.create(blobInfo, fileBytes);

        return objectName;
    }

    /**
     * Genera una nueva URL firmada con expiración renovada.
     * Uses an in-memory cache to avoid repeated IAM API calls.
     */
    public String regenerateSignedUrl(String objectName, int expirationHours) {
        Optional<String> cachedUrl = cachePort.getUrl(objectName);
        if (cachedUrl.isPresent()) {
            return cachedUrl.get();
        }

        String newUrl = generateSignedUrlInternal(objectName, expirationHours, this.credentials);

        // Cacheamos por (expirationHours - margen) para asegurar que no expire antes de
        // ser usada
        long ttlSeconds = (expirationHours - CACHE_EXPIRATION_MARGIN_HOURS) * 3600L;
        cachePort.cacheUrl(objectName, newUrl, ttlSeconds);

        return newUrl;
    }

    /**
     * Internal method to generate signed URL without caching.
     * Separated to allow the caching logic to be clean.
     */
    private String generateSignedUrlInternal(String objectName, int expirationHours, GoogleCredentials creds) {
        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, objectName).build();

        Storage.SignUrlOption signUrlOption;

        if (creds instanceof ServiceAccountCredentials) {
            signUrlOption = Storage.SignUrlOption.signWith((ServiceAccountSigner) creds);
        } else {
            String serviceAccountEmail = getServiceAccountEmail(creds);

            if (serviceAccountEmail != null) {
                signUrlOption = Storage.SignUrlOption.signWith(new CloudRunServiceAccountSigner(serviceAccountEmail));
            } else {
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
        String envEmail = System.getenv("GOOGLE_SERVICE_ACCOUNT_EMAIL");
        if (envEmail != null && !envEmail.isBlank()) {
            return envEmail;
        }

        if (creds instanceof com.google.auth.oauth2.ComputeEngineCredentials) {
            String email = ((com.google.auth.oauth2.ComputeEngineCredentials) creds).getAccount();
            if (email != null && !email.isBlank()) {
                return email;
            }
        }

        try {
            String metadataEmail = fetchEmailFromMetadataServer();
            if (metadataEmail != null && !metadataEmail.isBlank()) {
                return metadataEmail;
            }
        } catch (Exception e) {
            logger.warn("No se pudo obtener email de cuenta de servicio desde metadata: {}", e.getMessage());
            return null;
        }

        return null;
    }

    private String fetchEmailFromMetadataServer() throws IOException {
        String metadataUrl = "http://metadata.google.internal/computeMetadata/v1/instance/service-accounts/default/email";
        URL url = java.net.URI.create(metadataUrl).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Metadata-Flavor", "Google");
        connection.setConnectTimeout(2000);
        connection.setReadTimeout(2000);

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
            try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getErrorStream()))) {
                String inputLine;
                StringBuilder errorContent = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    errorContent.append(inputLine);
                }
            } catch (Exception e) {
                throw new IOException("Failed to fetch email from metadata server. HTTP error code: " + responseCode);
            }
            logger.warn("Metadata server devolvió código HTTP {} al solicitar email de servicio", responseCode);
            return null;
        }
    }

    public String regenerateSignedUrl(String objectName) {
        return regenerateSignedUrl(objectName, defaultUrlExpirationHours);
    }

    @Override
    public File get(String path) {
        if (path == null || path.isEmpty())
            return null;

        try {
            Blob blob = storage.get(BlobId.of(bucketName, path));
            if (blob == null) {
                logger.warn("Archivo no encontrado en GCS: {}", path);
                return null;
            }

            // Creamos un archivo temporal para procesarlo en el backend
            String extension = getExtension(path);
            File tempFile = File.createTempFile("gcs_migrate_", extension);
            blob.downloadTo(tempFile.toPath());
            
            return tempFile;
        } catch (IOException e) {
            logger.error("Error al descargar archivo de GCS para migración: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public String getUrl(String path) {
        if (path == null || path.isEmpty())
            return null;
        return regenerateSignedUrl(path);
    }

    /**
     * Elimina un objeto del bucket.
     */
    @Override
    public boolean delete(String objectName) {
        BlobId blobId = BlobId.of(bucketName, objectName);
        boolean deleted = storage.delete(blobId);
        if (deleted) {
            cachePort.evictUrl(objectName);
        } else {
            logger.warn("No se pudo eliminar el objeto (posiblemente no existe)");
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
                throw new RuntimeException("Error firmando blob vía IAM API para " + serviceAccountEmail, e);
            }
        }
    }
}
