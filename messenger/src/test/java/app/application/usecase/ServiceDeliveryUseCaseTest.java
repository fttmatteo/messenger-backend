package app.application.usecase;

import app.domain.model.Photo;
import app.domain.model.Plate;
import app.domain.model.ServiceDelivery;
import app.domain.model.Signature;
import app.domain.model.enums.Status;
import app.domain.ports.OcrPort;
import app.domain.ports.StoragePort;
import app.domain.services.CreateServiceDelivery;
import app.domain.services.DeleteServiceDelivery;
import app.domain.services.SearchServiceDelivery;
import app.domain.services.UpdateServiceDelivery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.io.File;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ServiceDeliveryUseCase Unit Tests")
class ServiceDeliveryUseCaseTest {

    @Mock
    private CreateServiceDelivery createService;

    @Mock
    private UpdateServiceDelivery updateService;

    @Mock
    private SearchServiceDelivery searchService;

    @Mock
    private DeleteServiceDelivery deleteService;

    @Mock
    private StoragePort storagePort;

    @Mock
    private OcrPort ocrPort;

    @InjectMocks
    private ServiceDeliveryUseCase serviceDeliveryUseCase;

    private ServiceDelivery sampleService;
    private File mockImageFile;

    @BeforeEach
    void setUp() {
        Plate plate = new Plate();
        plate.setPlateNumber("ABC123");

        sampleService = new ServiceDelivery();
        sampleService.setIdServiceDelivery(1L);
        sampleService.setPlate(plate);
        sampleService.setCurrentStatus(Status.ASSIGNED);

        mockImageFile = mock(File.class);
    }

    @Nested
    @DisplayName("Crear Servicio desde Imagen (OCR)")
    class CreateFromImageTests {

        @Test
        @DisplayName("Debe crear servicio con placa detectada por OCR")
        /**
         * Verifica el flujo completo de creación usando una imagen: OCR -> Guardar
         * imagen -> Crear servicio.
         */
        void shouldCreateServiceFromOcrDetection() throws Exception {
            when(ocrPort.extractText(mockImageFile)).thenReturn("ABC123");
            when(storagePort.save(eq(mockImageFile), eq("detections"), anyString()))
                    .thenReturn("/path/to/saved/image.png");

            when(createService.create(eq("ABC123"), anyString(), eq(1L), eq(123456L), isNull(), isNull()))
                    .thenReturn(sampleService);

            serviceDeliveryUseCase.createServiceFromImage(mockImageFile, 1L, 123456L, null, null);

            verify(ocrPort).extractText(mockImageFile);
            verify(storagePort).save(eq(mockImageFile), eq("detections"), contains("ABC123"));
            verify(createService).create(eq("ABC123"), anyString(), eq(1L), eq(123456L), isNull(), isNull());
        }

        @Test
        @DisplayName("Debe propagar excepción si OCR falla")
        void shouldPropagateExceptionIfOcrFails() throws Exception {
            when(ocrPort.extractText(mockImageFile)).thenThrow(new RuntimeException("Error en OCR"));

            assertThrows(RuntimeException.class,
                    () -> serviceDeliveryUseCase.createServiceFromImage(mockImageFile, 1L, 123456L, null, null));

            verify(createService, never()).create(anyString(), anyString(), anyLong(), anyLong(), any(), any());
        }
    }

    @Nested
    @DisplayName("Crear Servicio con Placa Manual")
    class CreateWithManualPlateTests {

        @Test
        @DisplayName("Debe crear servicio con placa manual")
        /**
         * Verifica la creación de servicio cuando la placa se ingresa manualmente
         * (bypass OCR).
         */
        void shouldCreateServiceWithManualPlate() throws Exception {
            when(storagePort.save(eq(mockImageFile), eq("detections"), anyString()))
                    .thenReturn("/path/to/saved/image.png");

            when(createService.create(eq("XYZ789"), anyString(), eq(1L), eq(123456L), isNull(), isNull()))
                    .thenReturn(sampleService);

            serviceDeliveryUseCase.createServiceWithManualPlate(mockImageFile, "XYZ789", 1L, 123456L, null, null);

            verify(ocrPort, never()).extractText(any());
            verify(storagePort).save(eq(mockImageFile), eq("detections"), contains("XYZ789"));
            verify(createService).create(eq("XYZ789"), anyString(), eq(1L), eq(123456L), isNull(), isNull());
        }
    }

    @Nested
    @DisplayName("Actualizar Estado")
    class UpdateStatusTests {

        @Test
        @DisplayName("Debe actualizar estado con firma y fotos")
        /**
         * Verifica la actualización de estado incluyendo evidencias (firma y fotos).
         */
        void shouldUpdateStatusWithSignatureAndPhotos() throws Exception {
            Signature signature = new Signature();
            signature.setSignaturePath("/path/signature.png");

            Photo photo = new Photo();
            photo.setPhotoPath("/path/photo.png");

            serviceDeliveryUseCase.updateStatus(1L, Status.DELIVERED, "Entregado OK",
                    signature, List.of(photo), 123456L);

            verify(updateService).updateStatus(
                    eq(1L), eq(Status.DELIVERED), eq("Entregado OK"),
                    eq(signature), anyList(), eq(123456L), isNull(), isNull());
        }

        @Test
        @DisplayName("Debe actualizar estado sin evidencias")
        void shouldUpdateStatusWithoutEvidence() throws Exception {
            serviceDeliveryUseCase.updateStatus(1L, Status.CANCELED, "Cancelado por cliente",
                    null, null, 123456L);

            verify(updateService).updateStatus(
                    eq(1L), eq(Status.CANCELED), eq("Cancelado por cliente"),
                    isNull(), isNull(), eq(123456L), isNull(), isNull());
        }
    }

    @Nested
    @DisplayName("Búsquedas")
    class SearchTests {

        @Test
        @DisplayName("Debe buscar por ID")
        /**
         * Verifica la búsqueda por ID delegada al servicio de dominio.
         */
        void shouldFindById() throws Exception {
            when(searchService.findById(1L)).thenReturn(sampleService);

            ServiceDelivery result = serviceDeliveryUseCase.findById(1L);

            assertNotNull(result);
            assertEquals(1L, result.getIdServiceDelivery());
        }

        @Test
        @DisplayName("Debe buscar todos los servicios")
        void shouldFindAll() {
            when(searchService.findAll()).thenReturn(List.of(sampleService));

            List<ServiceDelivery> result = serviceDeliveryUseCase.findAll();

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Debe buscar por placa")
        void shouldFindByPlate() {
            when(searchService.findByPlate("ABC123")).thenReturn(List.of(sampleService));

            List<ServiceDelivery> result = serviceDeliveryUseCase.findByPlate("ABC123");

            assertEquals(1, result.size());
        }
    }

    @Nested
    @DisplayName("Eliminar Servicio (Soft Delete)")
    class DeleteTests {

        @Test
        @DisplayName("Debe mover servicio a papelera por ID")
        /**
         * Verifica la eliminación lógica (soft delete).
         */
        void shouldSoftDeleteById() throws Exception {
            serviceDeliveryUseCase.deleteById(1L);

            verify(deleteService).deleteById(1L);
        }

        @Test
        @DisplayName("Debe mover servicio a papelera con registro de usuario")
        void shouldSoftDeleteByIdWithUser() throws Exception {
            serviceDeliveryUseCase.deleteById(1L, 123L);

            verify(deleteService).deleteById(1L, 123L);
        }
    }

    @Nested
    @DisplayName("Papelera (Trash)")
    class TrashTests {

        @Test
        @DisplayName("Debe listar servicios en papelera")
        /**
         * Verifica que se puedan recuperar los servicios eliminados.
         */
        void shouldFindDeleted() {
            ServiceDelivery deletedService = new ServiceDelivery();
            deletedService.setIdServiceDelivery(2L);
            deletedService.setDeleted(true);

            when(searchService.findDeleted()).thenReturn(List.of(deletedService));

            List<ServiceDelivery> result = serviceDeliveryUseCase.findDeleted();

            assertEquals(1, result.size());
            assertTrue(result.get(0).isDeleted());
        }

        @Test
        @DisplayName("Debe restaurar servicio desde papelera")
        /**
         * Verifica la restauración de un servicio eliminado.
         */
        void shouldRestoreFromTrash() throws Exception {
            ServiceDelivery restoredService = new ServiceDelivery();
            restoredService.setIdServiceDelivery(1L);
            restoredService.setDeleted(false);

            when(deleteService.restore(1L, 123L)).thenReturn(restoredService);

            ServiceDelivery result = serviceDeliveryUseCase.restore(1L, 123L);

            assertFalse(result.isDeleted());
            verify(deleteService).restore(1L, 123L);
        }
    }

    @Nested
    @DisplayName("Reasignación de Mensajero")
    class ReassignTests {

        @Test
        @DisplayName("Debe reasignar servicio a nuevo mensajero")
        /**
         * Verifica la reasignación de un servicio a otro empleado.
         */
        void shouldReassignMessenger() throws Exception {
            ServiceDelivery reassignedService = new ServiceDelivery();
            reassignedService.setIdServiceDelivery(1L);
            reassignedService.setCurrentStatus(Status.ASSIGNED);

            when(updateService.reassignMessenger(1L, 456L, 123L)).thenReturn(reassignedService);

            ServiceDelivery result = serviceDeliveryUseCase.reassignMessenger(1L, 456L, 123L);

            assertEquals(Status.ASSIGNED, result.getCurrentStatus());
            verify(updateService).reassignMessenger(1L, 456L, 123L);
        }
    }
}
