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

/**
 * Tests unitarios para ServiceDeliveryUseCase.
 * 
 * Verifica la orquestación correcta de operaciones de servicios de entrega,
 * incluyendo creación con OCR, actualización de estados y búsquedas.
 */
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
        void shouldCreateServiceFromOcrDetection() throws Exception {
            when(ocrPort.extractText(mockImageFile)).thenReturn("ABC123");
            when(storagePort.save(eq(mockImageFile), eq("detections"), anyString()))
                    .thenReturn("/path/to/saved/image.png");

            serviceDeliveryUseCase.createServiceFromImage(mockImageFile, 1L, 123456L);

            verify(ocrPort).extractText(mockImageFile);
            verify(storagePort).save(eq(mockImageFile), eq("detections"), contains("ABC123"));
            verify(createService).create(eq("ABC123"), anyString(), eq(1L), eq(123456L));
        }

        @Test
        @DisplayName("Debe propagar excepción si OCR falla")
        void shouldPropagateExceptionIfOcrFails() throws Exception {
            when(ocrPort.extractText(mockImageFile)).thenThrow(new RuntimeException("Error en OCR"));

            assertThrows(RuntimeException.class,
                    () -> serviceDeliveryUseCase.createServiceFromImage(mockImageFile, 1L, 123456L));

            verify(createService, never()).create(anyString(), anyString(), anyLong(), anyLong());
        }
    }

    @Nested
    @DisplayName("Crear Servicio con Placa Manual")
    class CreateWithManualPlateTests {

        @Test
        @DisplayName("Debe crear servicio con placa manual")
        void shouldCreateServiceWithManualPlate() throws Exception {
            when(storagePort.save(eq(mockImageFile), eq("detections"), anyString()))
                    .thenReturn("/path/to/saved/image.png");

            serviceDeliveryUseCase.createServiceWithManualPlate(mockImageFile, "XYZ789", 1L, 123456L);

            verify(ocrPort, never()).extractText(any());
            verify(storagePort).save(eq(mockImageFile), eq("detections"), contains("XYZ789"));
            verify(createService).create(eq("XYZ789"), anyString(), eq(1L), eq(123456L));
        }
    }

    @Nested
    @DisplayName("Actualizar Estado")
    class UpdateStatusTests {

        @Test
        @DisplayName("Debe actualizar estado con firma y fotos")
        void shouldUpdateStatusWithSignatureAndPhotos() throws Exception {
            Signature signature = new Signature();
            signature.setSignaturePath("/path/signature.png");

            Photo photo = new Photo();
            photo.setPhotoPath("/path/photo.png");

            serviceDeliveryUseCase.updateStatus(1L, Status.DELIVERED, "Entregado OK",
                    signature, List.of(photo), 123456L);

            verify(updateService).updateStatus(
                    eq(1L), eq(Status.DELIVERED), eq("Entregado OK"),
                    eq(signature), anyList(), eq(123456L));
        }

        @Test
        @DisplayName("Debe actualizar estado sin evidencias")
        void shouldUpdateStatusWithoutEvidence() throws Exception {
            serviceDeliveryUseCase.updateStatus(1L, Status.CANCELED, "Cancelado por cliente",
                    null, null, 123456L);

            verify(updateService).updateStatus(
                    eq(1L), eq(Status.CANCELED), eq("Cancelado por cliente"),
                    isNull(), isNull(), eq(123456L));
        }
    }

    @Nested
    @DisplayName("Búsquedas")
    class SearchTests {

        @Test
        @DisplayName("Debe buscar por ID")
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
        @DisplayName("Debe buscar por mensajero")
        void shouldFindByMessenger() {
            when(searchService.findByMessenger(123456L)).thenReturn(List.of(sampleService));

            List<ServiceDelivery> result = serviceDeliveryUseCase.findByMessenger(123456L);

            assertEquals(1, result.size());
            verify(searchService).findByMessenger(123456L);
        }

        @Test
        @DisplayName("Debe buscar por placa")
        void shouldFindByPlate() {
            when(searchService.findByPlate("ABC123")).thenReturn(List.of(sampleService));

            List<ServiceDelivery> result = serviceDeliveryUseCase.findByPlate("ABC123");

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Debe buscar por concesionario")
        void shouldFindByDealership() {
            when(searchService.findByDealership(1L)).thenReturn(List.of(sampleService));

            List<ServiceDelivery> result = serviceDeliveryUseCase.findByDealership(1L);

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Debe buscar por estado")
        void shouldFindByStatus() {
            when(searchService.findByStatus(Status.ASSIGNED)).thenReturn(List.of(sampleService));

            List<ServiceDelivery> result = serviceDeliveryUseCase.findByStatus(Status.ASSIGNED);

            assertEquals(1, result.size());
            assertEquals(Status.ASSIGNED, result.get(0).getCurrentStatus());
        }
    }

    @Nested
    @DisplayName("Eliminar Servicio")
    class DeleteTests {

        @Test
        @DisplayName("Debe eliminar servicio por ID")
        void shouldDeleteById() throws Exception {
            serviceDeliveryUseCase.deleteById(1L);

            verify(deleteService).deleteById(1L);
        }
    }
}
