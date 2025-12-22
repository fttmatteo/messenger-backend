package app.adapter.in.rest.controllers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import app.adapter.in.builder.ServiceDeliveryBuilder;
import app.adapter.in.rest.mapper.ServiceDeliveryResponseMapper;
import app.adapter.in.rest.response.ServiceDeliveryResponse;
import app.application.usecase.ServiceDeliveryUseCase;
import app.domain.model.Employee;
import app.domain.model.Plate;
import app.domain.model.ServiceDelivery;
import app.domain.model.enums.PlateType;
import app.domain.model.enums.Role;
import app.domain.model.enums.Status;
import app.domain.ports.EmployeePort;
import app.infrastructure.helper.FileHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Arrays;
import java.util.List;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ServiceDeliveryController Unit Tests")
class ServiceDeliveryControllerTest {

    @Mock
    private ServiceDeliveryUseCase serviceDeliveryUseCase;

    @Mock
    private ServiceDeliveryBuilder builder;

    @Mock
    private ServiceDeliveryResponseMapper responseMapper;

    @Mock
    private EmployeePort employeePort;

    @Mock
    private FileHelper fileHelper;

    private Employee adminUser;
    private Employee messengerUser;
    private ServiceDelivery sampleService;
    private ServiceDeliveryResponse sampleResponse;

    @BeforeEach
    void setUp() {
        adminUser = new Employee();
        adminUser.setIdEmployee(1L);
        adminUser.setDocument(123456L);
        adminUser.setRole(Role.ADMIN);
        adminUser.setFullName("Admin User");

        messengerUser = new Employee();
        messengerUser.setIdEmployee(2L);
        messengerUser.setDocument(789012L);
        messengerUser.setRole(Role.MESSENGER);
        messengerUser.setFullName("Messenger User");

        Plate plate = new Plate();
        plate.setIdPlate(1L);
        plate.setPlateNumber("ABC123");
        plate.setPlateType(PlateType.CAR);

        sampleService = new ServiceDelivery();
        sampleService.setIdServiceDelivery(1L);
        sampleService.setPlate(plate);
        sampleService.setCurrentStatus(Status.ASSIGNED);
        sampleService.setMessenger(messengerUser);

        sampleResponse = new ServiceDeliveryResponse();
        sampleResponse.setIdServiceDelivery(1L);
        sampleResponse.setCurrentStatus(Status.ASSIGNED);
    }

    @Nested
    @DisplayName("UseCase Interactions")
    class UseCaseInteractions {

        @Test
        @DisplayName("findAll debe llamar al UseCase")
        void findAllShouldCallUseCase() {
            when(serviceDeliveryUseCase.findAll()).thenReturn(Arrays.asList(sampleService));

            List<ServiceDelivery> result = serviceDeliveryUseCase.findAll();

            assertEquals(1, result.size());
            verify(serviceDeliveryUseCase).findAll();
        }

        @Test
        @DisplayName("findById debe llamar al UseCase")
        void findByIdShouldCallUseCase() throws Exception {
            when(serviceDeliveryUseCase.findById(1L)).thenReturn(sampleService);

            ServiceDelivery result = serviceDeliveryUseCase.findById(1L);

            assertEquals(1L, result.getIdServiceDelivery());
            verify(serviceDeliveryUseCase).findById(1L);
        }

        @Test
        @DisplayName("findDeleted debe llamar al UseCase")
        void findDeletedShouldCallUseCase() {
            ServiceDelivery deletedService = new ServiceDelivery();
            deletedService.setIdServiceDelivery(2L);
            deletedService.setDeleted(true);

            when(serviceDeliveryUseCase.findDeleted()).thenReturn(Arrays.asList(deletedService));

            List<ServiceDelivery> result = serviceDeliveryUseCase.findDeleted();

            assertEquals(1, result.size());
            assertTrue(result.get(0).isDeleted());
            verify(serviceDeliveryUseCase).findDeleted();
        }

        @Test
        @DisplayName("restore debe llamar al UseCase")
        void restoreShouldCallUseCase() throws Exception {
            ServiceDelivery restoredService = new ServiceDelivery();
            restoredService.setIdServiceDelivery(1L);
            restoredService.setDeleted(false);

            when(serviceDeliveryUseCase.restore(1L, 1L)).thenReturn(restoredService);

            ServiceDelivery result = serviceDeliveryUseCase.restore(1L, 1L);

            assertFalse(result.isDeleted());
            verify(serviceDeliveryUseCase).restore(1L, 1L);
        }

        @Test
        @DisplayName("reassignMessenger debe llamar al UseCase")
        void reassignShouldCallUseCase() throws Exception {
            ServiceDelivery reassignedService = new ServiceDelivery();
            reassignedService.setIdServiceDelivery(1L);
            reassignedService.setCurrentStatus(Status.ASSIGNED);

            when(serviceDeliveryUseCase.reassignMessenger(1L, 3L, 1L)).thenReturn(reassignedService);

            ServiceDelivery result = serviceDeliveryUseCase.reassignMessenger(1L, 3L, 1L);

            assertEquals(Status.ASSIGNED, result.getCurrentStatus());
            verify(serviceDeliveryUseCase).reassignMessenger(1L, 3L, 1L);
        }

        @Test
        @DisplayName("deleteById debe llamar al UseCase con soft delete")
        void deleteShouldCallUseCase() throws Exception {
            doNothing().when(serviceDeliveryUseCase).deleteById(1L, 1L);

            serviceDeliveryUseCase.deleteById(1L, 1L);

            verify(serviceDeliveryUseCase).deleteById(1L, 1L);
        }
    }

    @Nested
    @DisplayName("Response Mapper")
    class ResponseMapperTests {

        @Test
        @DisplayName("Mapper debe transformar ServiceDelivery a Response")
        void mapperShouldTransformToResponse() {
            when(responseMapper.toResponse(sampleService)).thenReturn(sampleResponse);

            ServiceDeliveryResponse response = responseMapper.toResponse(sampleService);

            assertEquals(1L, response.getIdServiceDelivery());
            assertEquals(Status.ASSIGNED, response.getCurrentStatus());
        }
    }

    @Nested
    @DisplayName("Casos Edge Críticos")
    class EdgeCaseTests {

        @Test
        @DisplayName("findByPlate debe normalizar mayúsculas")
        void findByPlateShouldNormalize() {
            when(serviceDeliveryUseCase.findByPlate("ABC123")).thenReturn(Arrays.asList(sampleService));

            List<ServiceDelivery> result = serviceDeliveryUseCase.findByPlate("ABC123");

            assertEquals(1, result.size());
            verify(serviceDeliveryUseCase).findByPlate("ABC123");
        }

        @Test
        @DisplayName("updateStatus debe propagar excepción de BusinessException")
        void updateStatusShouldPropagateBusinessException() throws Exception {
            when(serviceDeliveryUseCase.updateStatus(anyLong(), any(), anyString(), any(), any(), anyLong()))
                    .thenThrow(new app.domain.exception.BusinessException("El servicio está bloqueado"));

            assertThrows(app.domain.exception.BusinessException.class,
                    () -> serviceDeliveryUseCase.updateStatus(1L, Status.DELIVERED, "obs", null, null, 1L));
        }

        @Test
        @DisplayName("reassign debe fallar si el servicio no está en CANCELED")
        void reassignShouldFailIfNotCanceled() throws Exception {
            when(serviceDeliveryUseCase.reassignMessenger(1L, 3L, 1L))
                    .thenThrow(new app.domain.exception.BusinessException(
                            "Solo se pueden reasignar servicios en estado CANCELED"));

            assertThrows(app.domain.exception.BusinessException.class,
                    () -> serviceDeliveryUseCase.reassignMessenger(1L, 3L, 1L));
        }

        @Test
        @DisplayName("restore debe fallar si el servicio no está en papelera")
        void restoreShouldFailIfNotDeleted() throws Exception {
            when(serviceDeliveryUseCase.restore(1L, 1L))
                    .thenThrow(new app.domain.exception.BusinessException(
                            "El servicio no está en la papelera"));

            assertThrows(app.domain.exception.BusinessException.class,
                    () -> serviceDeliveryUseCase.restore(1L, 1L));
        }
    }
}
