package app.adapter.in.rest.controllers;
import app.adapter.in.rest.delivery.ServiceDeliveryController;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import app.adapter.in.builder.ServiceDeliveryBuilder;
import app.adapter.in.rest.delivery.ServiceDeliveryResponseMapper;
import app.adapter.in.rest.delivery.ServiceDeliveryResponse;
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

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas unitarias de ServiceDeliveryController")
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
        plate.setPlateNumber("ABC1234567");
        plate.setPlateType(PlateType.MOTORCYCLE);

        sampleService = new ServiceDelivery();
        sampleService.setIdServiceDelivery(1L);
        sampleService.setUuid("550e8400-e29b-41d4-a716-446655440000");
        sampleService.setPlate(plate);
        sampleService.setCurrentStatus(Status.ASSIGNED);
        sampleService.setMessenger(messengerUser);

        sampleResponse = new ServiceDeliveryResponse();
        sampleResponse.setIdServiceDelivery(1L);
        sampleResponse.setUuid("550e8400-e29b-41d4-a716-446655440000");
        sampleResponse.setCurrentStatus(Status.ASSIGNED);
    }

    @Nested
    @DisplayName("Debe realizar borrado lógico")
    class DeleteTests {

        @Test
        @DisplayName("Debe retornar 200 al eliminar lógicamente")
        void shouldSoftDelete() throws Exception {
            Employee admin = new Employee();
            admin.setIdEmployee(1L);
            admin.setRole(Role.ADMIN);

            when(securityHelper.getCurrentUser()).thenReturn(admin);
            when(serviceDeliveryUseCase.findByUuid("550e8400-e29b-41d4-a716-446655440000")).thenReturn(sampleService);

            mockMvc.perform(delete("/services/deleteService/550e8400-e29b-41d4-a716-446655440000"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").exists());

            verify(serviceDeliveryUseCase).deleteById(1L, 1L);
        }
    }

    @Nested
    @DisplayName("Debe restaurar desde la papelera")
    class RestoreTests {

        @Test
        @DisplayName("Debe restaurar servicio desde papelera")
        void shouldRestoreFromTrash() throws Exception {
            Employee admin = new Employee();
            admin.setIdEmployee(1L);
            admin.setRole(Role.ADMIN);

            when(securityHelper.getCurrentUser()).thenReturn(admin);
            when(serviceDeliveryUseCase.findByUuidIncludingDeleted("550e8400-e29b-41d4-a716-446655440000")).thenReturn(sampleService);
            when(serviceDeliveryUseCase.restore(1L, 1L)).thenReturn(sampleService);
            when(responseMapper.toResponse(sampleService)).thenReturn(sampleResponse);
 
            mockMvc.perform(post("/services/trash/restore/550e8400-e29b-41d4-a716-446655440000"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.idServiceDelivery").value(1L));

        }
    }

    @Nested
    @DisplayName("Creación de servicios")
    class CreateServiceTests {

        @Test
        @DisplayName("Debe parsear scheduledAt y pasar la fecha al caso de uso")
        void shouldParseScheduledAtAndPassToUseCase() throws Exception {
            Employee admin = new Employee();
            admin.setIdEmployee(1L);
            admin.setRole(Role.ADMIN);

            when(securityHelper.getCurrentUser()).thenReturn(admin);

            app.adapter.in.rest.delivery.ServiceDeliveryCreateRequest mockRequest = 
                new app.adapter.in.rest.delivery.ServiceDeliveryCreateRequest("1", "2");
            mockRequest.setOriginDealershipId("2");
            mockRequest.setManualPlateNumber("ABC1234567");
            
            ServiceDeliveryBuilder.ServiceDeliveryCreateData mockData = 
                new ServiceDeliveryBuilder.ServiceDeliveryCreateData(1L, 2L, 2L);
            
            when(builder.buildCreateData(any())).thenReturn(mockData);

            ServiceDelivery mockCreatedService = new ServiceDelivery();
            mockCreatedService.setIdServiceDelivery(100L);
            when(serviceDeliveryUseCase.createServiceWithManualPlate(
                    anyString(), anyLong(), anyLong(), anyLong(), 
                    any(), any(), any(java.time.LocalDateTime.class)))
                .thenReturn(mockCreatedService);

            when(responseMapper.toResponse(mockCreatedService)).thenReturn(sampleResponse);

            mockMvc.perform(post("/services/createService")
                    .param("dealershipId", "1")
                    .param("originDealershipId", "2")
                    .param("messengerId", "2")
                    .param("manualPlateNumber", "ABC1234567")
                    .param("scheduledAt", "2050-01-01T12:00:00Z"))
                    .andExpect(status().isCreated());

            verify(serviceDeliveryUseCase).createServiceWithManualPlate(
                eq("ABC1234567"), 
                eq(1L), 
                eq(2L), 
                eq(2L), 
                isNull(), 
                isNull(), 
                argThat(date -> date != null && date.getYear() == 2050)
            );
        }
    }

}
