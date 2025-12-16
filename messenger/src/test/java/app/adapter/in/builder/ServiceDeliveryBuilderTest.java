package app.adapter.in.builder;

import app.adapter.in.rest.request.ServiceDeliveryCreateRequest;
import app.adapter.in.rest.request.ServiceDeliveryUpdateStatusRequest;
import app.adapter.in.validators.ServiceDeliveryValidator;
import app.application.exceptions.InputsException;
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

/**
 * Tests unitarios para ServiceDeliveryBuilder.
 * 
 * Verifica la construcción de objetos de datos validados para servicios.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ServiceDeliveryBuilder Unit Tests")
class ServiceDeliveryBuilderTest {

    @Mock
    private ServiceDeliveryValidator validator;

    @InjectMocks
    private ServiceDeliveryBuilder builder;

    @Nested
    @DisplayName("buildCreateData")
    class BuildCreateDataTests {

        @Test
        @DisplayName("Debe construir datos de creación válidos")
        void shouldBuildValidCreateData() throws Exception {
            ServiceDeliveryCreateRequest request = new ServiceDeliveryCreateRequest();
            request.setDealershipId("1");
            request.setMessengerDocument("123456789");

            when(validator.idValidator("1")).thenReturn(1L);
            when(validator.documentValidator("123456789")).thenReturn(123456789L);

            ServiceDeliveryBuilder.ServiceDeliveryCreateData result = builder.buildCreateData(request);

            assertNotNull(result);
            assertEquals(1L, result.getDealershipId());
            assertEquals(123456789L, result.getMessengerDocument());
        }

        @Test
        @DisplayName("Debe propagar excepción si ID es inválido")
        void shouldPropagateExceptionForInvalidId() throws Exception {
            ServiceDeliveryCreateRequest request = new ServiceDeliveryCreateRequest();
            request.setDealershipId("invalid");
            request.setMessengerDocument("123456789");

            when(validator.idValidator("invalid"))
                    .thenThrow(new InputsException("ID inválido"));

            assertThrows(InputsException.class, () -> builder.buildCreateData(request));
        }

        @Test
        @DisplayName("Debe propagar excepción si documento es inválido")
        void shouldPropagateExceptionForInvalidDocument() throws Exception {
            ServiceDeliveryCreateRequest request = new ServiceDeliveryCreateRequest();
            request.setDealershipId("1");
            request.setMessengerDocument("abc");

            when(validator.idValidator("1")).thenReturn(1L);
            when(validator.documentValidator("abc"))
                    .thenThrow(new InputsException("Documento inválido"));

            assertThrows(InputsException.class, () -> builder.buildCreateData(request));
        }
    }

    @Nested
    @DisplayName("buildUpdateStatusData")
    class BuildUpdateStatusDataTests {

        @Test
        @DisplayName("Debe construir datos de actualización válidos")
        void shouldBuildValidUpdateData() throws Exception {
            ServiceDeliveryUpdateStatusRequest request = new ServiceDeliveryUpdateStatusRequest();
            request.setStatus("DELIVERED");
            request.setObservation("Entregado correctamente");
            request.setUserDocument("123456789");

            when(validator.statusValidator("DELIVERED")).thenReturn(Status.DELIVERED);
            when(validator.observationValidator("Entregado correctamente")).thenReturn("Entregado correctamente");
            when(validator.documentValidator("123456789")).thenReturn(123456789L);

            ServiceDeliveryBuilder.ServiceDeliveryUpdateData result = builder.buildUpdateStatusData(request);

            assertNotNull(result);
            assertEquals(Status.DELIVERED, result.getStatus());
            assertEquals("Entregado correctamente", result.getObservation());
            assertEquals(123456789L, result.getUserDocument());
        }

        @Test
        @DisplayName("Debe propagar excepción si estado es inválido")
        void shouldPropagateExceptionForInvalidStatus() throws Exception {
            ServiceDeliveryUpdateStatusRequest request = new ServiceDeliveryUpdateStatusRequest();
            request.setStatus("INVALID");
            request.setObservation("Test");
            request.setUserDocument("123456789");

            when(validator.statusValidator("INVALID"))
                    .thenThrow(new InputsException("Estado inválido"));

            assertThrows(InputsException.class, () -> builder.buildUpdateStatusData(request));
        }
    }

    @Nested
    @DisplayName("Data Classes")
    class DataClassesTests {

        @Test
        @DisplayName("ServiceDeliveryCreateData debe ser inmutable")
        void createDataShouldBeImmutable() {
            ServiceDeliveryBuilder.ServiceDeliveryCreateData data = new ServiceDeliveryBuilder.ServiceDeliveryCreateData(
                    1L, 123456789L);

            assertEquals(1L, data.getDealershipId());
            assertEquals(123456789L, data.getMessengerDocument());
        }

        @Test
        @DisplayName("ServiceDeliveryUpdateData debe ser inmutable")
        void updateDataShouldBeImmutable() {
            ServiceDeliveryBuilder.ServiceDeliveryUpdateData data = new ServiceDeliveryBuilder.ServiceDeliveryUpdateData(
                    Status.DELIVERED, "Obs", 123456789L);

            assertEquals(Status.DELIVERED, data.getStatus());
            assertEquals("Obs", data.getObservation());
            assertEquals(123456789L, data.getUserDocument());
        }
    }
}
