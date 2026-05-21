package app.application.usecase;

import app.domain.model.Photo;
import app.domain.model.Plate;
import app.domain.model.ServiceDelivery;
import app.domain.model.Signature;
import app.domain.model.enums.Status;

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
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas unitarias de ServiceDeliveryUseCase")
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
    @InjectMocks
    private ServiceDeliveryUseCase serviceDeliveryUseCase;

    private ServiceDelivery sampleService;

    @BeforeEach
    void setUp() {
        Plate plate = new Plate();
        plate.setPlateNumber("ABC1234567");

        sampleService = new ServiceDelivery();
        sampleService.setIdServiceDelivery(1L);
        sampleService.setPlate(plate);
        sampleService.setCurrentStatus(Status.ASSIGNED);

    }


    @Nested
    @DisplayName("Crear Servicio con Placa Manual")
    class CreateWithManualPlateTests {

        @Test
        @DisplayName("Debe crear servicio con placa manual")
        void shouldCreateServiceWithManualPlate() throws Exception {
            when(createService.create(eq("XYZ789"), eq(1L), eq(2L), eq(123456L), isNull(), isNull()))
                    .thenReturn(sampleService);

            serviceDeliveryUseCase.createServiceWithManualPlate("XYZ789", 1L, 2L, 123456L, null, null);

            verify(createService).create(eq("XYZ789"), eq(1L), eq(2L), eq(123456L), isNull(), isNull());
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
                    eq(signature), anyList(), eq(123456L), isNull(), isNull());
        }

        @Test
        @DisplayName("Debe actualizar el estado sin evidencia")

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
        void shouldFindById() throws Exception {
            when(searchService.findById(1L)).thenReturn(sampleService);

            ServiceDelivery result = serviceDeliveryUseCase.findById(1L);

            assertNotNull(result);
            assertEquals(1L, result.getIdServiceDelivery());
        }

        @Test
        @DisplayName("Debe mapear los campos de ordenación en la paginación")

        void shouldMapSortFieldsInPagination() {
            serviceDeliveryUseCase.findAllPaginated(0, 10, "messengerName", "asc", null, null);
            verify(searchService).findAllPaginated(isNull(), eq(false), isNull(),
                    argThat(pageable -> pageable.getSort().getOrderFor("messenger.fullName") != null &&
                            pageable.getSort().getOrderFor("messenger.fullName").isAscending()));

            serviceDeliveryUseCase.findByMessengerPaginated(123L, 0, 10, "plateNumber", "desc", null, null);
            verify(searchService).findByMessengerPaginated(eq(123L), isNull(), eq(false), isNull(),
                    argThat(pageable -> pageable.getSort().getOrderFor("plate.plateNumber") != null &&
                            pageable.getSort().getOrderFor("plate.plateNumber").isDescending()));

            serviceDeliveryUseCase.findAllPaginated(0, 10, "dealershipName", "desc", null, null);
            verify(searchService).findAllPaginated(isNull(), eq(false), isNull(),
                    argThat(pageable -> pageable.getSort().getOrderFor("dealership.name") != null &&
                            pageable.getSort().getOrderFor("dealership.name").isDescending()));
        }
    }

    @Nested
    @DisplayName("Eliminar Servicio (Soft Delete)")
    class DeleteTests {

        @Test
        @DisplayName("Debe mover servicio a papelera por ID")
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
    @DisplayName("Debe buscar eliminados")
    class TrashTests {

        @Test
        @DisplayName("Debe listar servicios en papelera")
        void shouldFindDeleted() {
            ServiceDelivery deletedService = new ServiceDelivery();
            deletedService.setIdServiceDelivery(2L);
            deletedService.setDeleted(true);
            org.springframework.data.domain.Page<ServiceDelivery> page = new org.springframework.data.domain.PageImpl<>(java.util.List.of(deletedService));

            when(searchService.findDeleted(org.mockito.ArgumentMatchers.any(org.springframework.data.domain.Pageable.class))).thenReturn(page);

            org.springframework.data.domain.Page<ServiceDelivery> result = serviceDeliveryUseCase.findDeleted(org.springframework.data.domain.PageRequest.of(0, 10));

            assertEquals(1, result.getContent().size());
            assertTrue(result.getContent().get(0).isDeleted());
        }

        @Test
        @DisplayName("Debe restaurar desde la papelera")

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
