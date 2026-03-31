package app.adapter.out.storage.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
@ConditionalOnProperty(name = "app.storage.type", havingValue = "gcs")
public class GcsConfig {

    @Value("${google.cloud.storage.project-id}")
    private String projectId;

    @Bean
    public GoogleCredentials googleCredentials() throws IOException {
        return GoogleCredentials.getApplicationDefault();
    }

    @Bean
    public Storage googleCloudStorage(GoogleCredentials credentials) throws IOException {
        return StorageOptions.newBuilder()
                .setProjectId(projectId)
                .setCredentials(credentials)
                .build()
                .getService();
    }
}
