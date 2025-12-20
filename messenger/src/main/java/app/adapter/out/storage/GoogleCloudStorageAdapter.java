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

@Component
@ConditionalOnProperty(name = "app.storage.type", havingValue = "gcs")
public class GoogleCloudStorageAdapter implements StoragePort {

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
        this.credentials = GoogleCredentials.getApplicationDefault();
        this.storage = StorageOptions.newBuilder()
                .setProjectId(projectId)
                .setCredentials(credentials)
                .build()
                .getService();
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

        return objectName;
    }

    public String regenerateSignedUrl(String objectName, int expirationHours) {
        return regenerateSignedUrl(objectName, expirationHours, this.credentials);
    }

    private String regenerateSignedUrl(String objectName, int expirationHours, GoogleCredentials creds) {
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
            try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getErrorStream()))) {
                String inputLine;
                StringBuilder errorContent = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    errorContent.append(inputLine);
                }
            } catch (Exception e) {
                throw new IOException("Failed to fetch email from metadata server. HTTP error code: " + responseCode);
            }
            return null;
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
        BlobId blobId = BlobId.of(bucketName, objectName);
        boolean deleted = storage.delete(blobId);
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
