package app.adapter.in.rest.controllers;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
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
import app.infrastructure.helper.FileHelper;
import app.infrastructure.helper.SecurityHelper;
import app.domain.services.FileValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
@DisplayName("ServiceDeliveryController Unit Tests")
class ServiceDeliveryControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ServiceDeliveryUseCase serviceDeliveryUseCase;

    @Mock
    private ServiceDeliveryBuilder builder;

    @Mock
    private ServiceDeliveryResponseMapper responseMapper;

    @Mock
    private SecurityHelper securityHelper;

    @Mock
    private FileHelper fileHelper;

    @Mock
    private FileValidationService fileValidationService;

    @InjectMocks
    private ServiceDeliveryController controller;

    private Employee messengerUser;
    private ServiceDelivery sampleService;
    private ServiceDeliveryResponse sampleResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new org.springframework.http.converter.json.MappingJackson2HttpMessageConverter())
                .build();

        messengerUser = new Employee();
        messengerUser.setIdEmployee(2L);
        messengerUser.setRole(Role.MESSENGER);

        Plate plate = new Plate();
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
    @DisplayName("Endpoint DELETE /services/deleteService/{id}")
    class DeleteTests {

        @Test
        @DisplayName("Debe retornar 200 al eliminar lógicamente")
        /**
         * Verifica que el endpoint de eliminación lógica retorne 200.
         */
        void shouldSoftDelete() throws Exception {
            Employee admin = new Employee();
            admin.setIdEmployee(1L);
            admin.setRole(Role.ADMIN);

            when(securityHelper.getCurrentUser()).thenReturn(admin);

            mockMvc.perform(delete("/services/deleteService/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").exists());

            verify(serviceDeliveryUseCase).deleteById(1L, 1L);
        }
    }

    @Nested
    @DisplayName("Endpoint POST /services/trash/restore/{id}")
    class RestoreTests {

        @Test
        @DisplayName("Debe restaurar servicio desde papelera")
        /**
         * Verifica que el endpoint de restauración retorne 200.
         */
        void shouldRestoreFromTrash() throws Exception {
            Employee admin = new Employee();
            admin.setIdEmployee(1L);
            admin.setRole(Role.ADMIN);

            when(securityHelper.getCurrentUser()).thenReturn(admin);
            when(serviceDeliveryUseCase.restore(1L, 1L)).thenReturn(sampleService);
            when(responseMapper.toResponse(sampleService)).thenReturn(sampleResponse);

            mockMvc.perform(post("/services/trash/restore/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.idServiceDelivery").value(1L));

        }
    }

    @Nested
    @DisplayName("Endpoint POST /services/extractPlate")
    class ExtractPlateTests {

        @Test
        @DisplayName("Debe extraer placa exitosamente")
        /**
         * Verifica que el endpoint de extracción de placa retorne 200.
         */
        void shouldExtractPlate() throws Exception {
            MockMultipartFile file = new MockMultipartFile("image", "test.jpg", "image/jpeg", "test".getBytes());

            doNothing().when(fileValidationService).validateImageFile(any());
            when(fileHelper.withTempFile(any(), any())).thenAnswer(invocation -> {
                FileHelper.FileOperation<?> operation = invocation.getArgument(1);
                return operation.execute(new java.io.File("test.jpg"));
            });
            when(serviceDeliveryUseCase.extractPlateFromImage(any())).thenReturn("ABC123");

            mockMvc.perform(multipart("/services/extractPlate").file(file))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.plate").value("ABC123"))
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("Debe retornar fallo si OCR no detecta nada")
        /**
         * Verifica que el endpoint de extracción de placa retorne 200.
         */
        void shouldReturnFailureIfOcrFails() throws Exception {
            MockMultipartFile file = new MockMultipartFile("image", "test.jpg", "image/jpeg", "test".getBytes());

            doNothing().when(fileValidationService).validateImageFile(any());
            when(fileHelper.withTempFile(any(), any())).thenAnswer(invocation -> {
                FileHelper.FileOperation<?> operation = invocation.getArgument(1);
                return operation.execute(new java.io.File("test.jpg"));
            });
            when(serviceDeliveryUseCase.extractPlateFromImage(any())).thenReturn("");

            mockMvc.perform(multipart("/services/extractPlate").file(file))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.plate").isEmpty());
        }
    }
}
