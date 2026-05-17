package app.adapter.out.storage;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import app.domain.ports.StorageCachePort;
import app.infrastructure.storage.ImageOptimizer;
import app.support.AbstractIntegrationTest;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@DisplayName("Pruebas unitarias de StorageProvider Integration")
class StorageProviderIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private LocalStorageAdapter localStorageAdapter;

    @Autowired
    private ImageOptimizer imageOptimizer;

    private File tempFile;

    @BeforeEach
    void setUp() throws IOException {
        tempFile = File.createTempFile("test-image", ".jpg");
        Files.write(tempFile.toPath(), "test-content".getBytes());
    }

    @AfterEach
    void tearDown() {
        if (tempFile != null && tempFile.exists()) {
            tempFile.delete();
        }
        FileSystemUtils.deleteRecursively(localStorageAdapter.getStoragePath().toFile());
    }

    @Test
    @DisplayName("Debe guardar y eliminar localmente")

    void shouldSaveAndDeleteLocal() throws IOException {
        String subDir = "test-folder";
        String customName = "my-photo";

        String savedPath = localStorageAdapter.save(tempFile, subDir, customName);
        assertNotNull(savedPath);
        assertTrue(savedPath.contains(subDir));
        assertTrue(savedPath.contains(customName));

        File physicalFile = localStorageAdapter.get(savedPath);
        assertNotNull(physicalFile);
        assertTrue(physicalFile.exists());

        boolean deleted = localStorageAdapter.delete(savedPath);
        assertTrue(deleted);
        assertTrue(!physicalFile.exists());
    }

    @Test
    @DisplayName("Debe llamar al servicio GCS")

    void shouldCallGcsService() throws IOException {
    
        Storage mockStorage = mock(Storage.class);
        StorageCachePort mockCache = mock(StorageCachePort.class);
        
        GoogleCredentials mockCredentials = mock(GoogleCredentials.class);
        
        GoogleCloudStorageAdapter gcsAdapter = new GoogleCloudStorageAdapter(
                "test-bucket", 24, imageOptimizer, mockCache, mockStorage, mockCredentials
        );

        gcsAdapter.save(tempFile, "avatars", "user-1");
        
        verify(mockStorage).create(any(), any(byte[].class));
    }
}
