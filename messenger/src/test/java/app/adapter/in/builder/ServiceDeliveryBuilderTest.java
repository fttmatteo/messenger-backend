package app.adapter.in.builder;

import app.adapter.in.rest.request.ServiceDeliveryCreateRequest;
import app.adapter.in.rest.request.ServiceDeliveryUpdateStatusRequest;
import app.adapter.in.rest.validators.ServiceDeliveryValidator;
import app.domain.exception.InputsException;
import app.domain.model.enums.Status;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas unitarias de ServiceDeliveryBuilder")
class ServiceDeliveryBuilderTest {

    @Mock
    private ServiceDeliveryValidator validator;

    @InjectMocks
    private ServiceDeliveryBuilder builder;

    @Nested
    @DisplayName("Debe construir datos de creación válidos")
    class BuildCreateDataTests {

        @Test
        @DisplayName("Debe construir datos de creación válidos")
        void shouldBuildValidCreateData() throws Exception {
            ServiceDeliveryCreateRequest request = new ServiceDeliveryCreateRequest();
            request.setDealershipId("1");
            request.setOriginDealershipId("2");
            request.setMessengerId("100");

            when(validator.idValidator("1")).thenReturn(1L);
            when(validator.idValidator("2")).thenReturn(2L);
            when(validator.idValidator("100")).thenReturn(100L);

            ServiceDeliveryBuilder.ServiceDeliveryCreateData result = builder.buildCreateData(request);

            assertNotNull(result);
            assertEquals(1L, result.getDealershipId());
            assertEquals(2L, result.getOriginDealershipId());
            assertEquals(100L, result.getMessengerId());
        }

        @Test
        @DisplayName("Debe propagar excepción para ID inválido")

        void shouldPropagateExceptionForInvalidId() throws Exception {
            ServiceDeliveryCreateRequest request = new ServiceDeliveryCreateRequest();
            request.setDealershipId("invalid");
            request.setOriginDealershipId("2");
            request.setMessengerId("100");

            when(validator.idValidator("invalid"))
                    .thenThrow(new InputsException("ID inválido"));

            assertThrows(InputsException.class, () -> builder.buildCreateData(request));
        }

        @Test
        @DisplayName("Debe propagar excepción para ID de mensajero inválido")

        void shouldPropagateExceptionForInvalidMessengerId() throws Exception {
            ServiceDeliveryCreateRequest request = new ServiceDeliveryCreateRequest();
            request.setDealershipId("1");
            request.setOriginDealershipId("2");
            request.setMessengerId("abc");

            when(validator.idValidator("1")).thenReturn(1L);
            when(validator.idValidator("2")).thenReturn(2L);
            when(validator.idValidator("abc"))
                    .thenThrow(new InputsException("ID inválido"));

            assertThrows(InputsException.class, () -> builder.buildCreateData(request));
        }
    }

    @Nested
    @DisplayName("Debe construir datos de actualización válidos")
    class BuildUpdateStatusDataTests {

        @Test
        @DisplayName("Debe construir datos de actualización válidos")
        void shouldBuildValidUpdateData() throws Exception {
            ServiceDeliveryUpdateStatusRequest request = new ServiceDeliveryUpdateStatusRequest();
            request.setStatus("DELIVERED");
            request.setObservation("Entregado correctamente");
            request.setUserId("100");

            when(validator.statusValidator("DELIVERED")).thenReturn(Status.DELIVERED);
            when(validator.observationValidator("Entregado correctamente")).thenReturn("Entregado correctamente");
            when(validator.idValidator("100")).thenReturn(100L);

            ServiceDeliveryBuilder.ServiceDeliveryUpdateData result = builder.buildUpdateStatusData(request);

            assertNotNull(result);
            assertEquals(Status.DELIVERED, result.getStatus());
            assertEquals("Entregado correctamente", result.getObservation());
            assertEquals(100L, result.getUserId());
        }

        @Test
        @DisplayName("Debe propagar excepción para estado inválido")

        void shouldPropagateExceptionForInvalidStatus() throws Exception {
            ServiceDeliveryUpdateStatusRequest request = new ServiceDeliveryUpdateStatusRequest();
            request.setStatus("INVALID");
            request.setObservation("Test");
            request.setUserId("100");

            when(validator.statusValidator("INVALID"))
                    .thenThrow(new InputsException("Estado inválido"));

            assertThrows(InputsException.class, () -> builder.buildUpdateStatusData(request));
        }
    }

    @Nested
    @DisplayName("Los datos de creación deben ser inmutables")
    class DataClassesTests {

        @Test
        @DisplayName("ServiceDeliveryCreateData debe ser inmutable")
        void createDataShouldBeImmutable() {
            ServiceDeliveryBuilder.ServiceDeliveryCreateData data = new ServiceDeliveryBuilder.ServiceDeliveryCreateData(
                    1L, 2L, 100L);

            assertEquals(1L, data.getDealershipId());
            assertEquals(2L, data.getOriginDealershipId());
            assertEquals(100L, data.getMessengerId());
        }

        @Test
        @DisplayName("Los datos de actualización deben ser inmutables")

        void updateDataShouldBeImmutable() {
            ServiceDeliveryBuilder.ServiceDeliveryUpdateData data = new ServiceDeliveryBuilder.ServiceDeliveryUpdateData(
                    Status.DELIVERED, "Obs", 100L);

            assertEquals(Status.DELIVERED, data.getStatus());
            assertEquals("Obs", data.getObservation());
            assertEquals(100L, data.getUserId());
        }
    }
}
