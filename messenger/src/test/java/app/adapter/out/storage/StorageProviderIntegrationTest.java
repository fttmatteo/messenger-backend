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

/**
 * Pruebas de integración para proveedores de almacenamiento.
 * Verifica LocalStorageAdapter (real) y GoogleCloudStorageAdapter (mockeado).
 */
@DisplayName("Storage Provider Integration Tests")
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
        // Limpiar directorio de uploads de test
        FileSystemUtils.deleteRecursively(localStorageAdapter.getStoragePath().toFile());
    }

    @Test
    @DisplayName("Should save and delete file in LocalStorageAdapter")
    void shouldSaveAndDeleteLocal() throws IOException {
        String subDir = "test-folder";
        String customName = "my-photo";

        // Guardar
        String savedPath = localStorageAdapter.save(tempFile, subDir, customName);
        assertNotNull(savedPath);
        assertTrue(savedPath.contains(subDir));
        assertTrue(savedPath.contains(customName));

        // Verificar existencia física
        File physicalFile = localStorageAdapter.get(savedPath);
        assertNotNull(physicalFile);
        assertTrue(physicalFile.exists());

        // Eliminar
        boolean deleted = localStorageAdapter.delete(savedPath);
        assertTrue(deleted);
        assertTrue(!physicalFile.exists());
    }

    @Test
    @DisplayName("Should call GCS storage service in GoogleCloudStorageAdapter")
    void shouldCallGcsService() throws IOException {
        // Dado que GCS requiere credenciales reales, usamos un Mock del servicio Storage
        // para validar que el adaptador llama a los métodos correctos de la SDK de Google.
        Storage mockStorage = mock(Storage.class);
        StorageCachePort mockCache = mock(StorageCachePort.class);
        
        GoogleCredentials mockCredentials = mock(GoogleCredentials.class);
        
        GoogleCloudStorageAdapter gcsAdapter = new GoogleCloudStorageAdapter(
                "test-bucket", 24, imageOptimizer, mockCache, mockStorage, mockCredentials
        );

        // Al guardar, debe llamar a storage.create
        gcsAdapter.save(tempFile, "avatars", "user-1");
        
        verify(mockStorage).create(any(), any(byte[].class));
    }
}
