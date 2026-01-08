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

import java.util.Arrays;

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
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

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
    @DisplayName("Endpoint GET /services/allServices")
    class FindAllTests {

        @Test
        @DisplayName("Debe retornar 200 y lista de servicios")
        void shouldReturnAllServices() throws Exception {
            when(securityHelper.getCurrentUser()).thenReturn(messengerUser);
            when(serviceDeliveryUseCase.findAll()).thenReturn(Arrays.asList(sampleService));
            when(responseMapper.toResponse(sampleService)).thenReturn(sampleResponse);

            mockMvc.perform(get("/services/allServices"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].idServiceDelivery").value(1L))
                    .andExpect(jsonPath("$[0].currentStatus").value("ASSIGNED"));

            verify(serviceDeliveryUseCase).findAll();
        }
    }

    @Nested
    @DisplayName("Endpoint DELETE /services/deleteService/{id}")
    class DeleteTests {

        @Test
        @DisplayName("Debe retornar 200 al eliminar lógicamente")
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

            verify(serviceDeliveryUseCase).restore(1L, 1L);
        }
    }
}
