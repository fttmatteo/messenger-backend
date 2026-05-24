package app.adapter.in.rest.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import app.adapter.in.rest.tracking.LiveTrackingRequest;
import app.adapter.in.rest.location.BulkLocationsRequest;
import app.domain.model.enums.Role;
import app.domain.model.enums.TrackingStatus;
import app.adapter.out.persistence.entities.EmployeeEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import app.support.AbstractIntegrationTest;

@Transactional
@DisplayName("Pruebas unitarias de TrackingController Integration")
class TrackingControllerIntegrationTest extends AbstractIntegrationTest {

        @Autowired
        private WebApplicationContext context;

        @Autowired
        private EntityManager entityManager;

        @MockitoBean
        private app.domain.ports.TrackingPort trackingPort;

        private ObjectMapper objectMapper = new ObjectMapper();

        private MockMvc mockMvc;

        @BeforeEach
        public void setup() {
                mockMvc = MockMvcBuilders
                                .webAppContextSetup(context)
                                .apply(springSecurity())
                                .build();
        }

        @Test
        @DisplayName("Debe actualizar ubicación exitosamente")
@WithMockUser(roles = "MESSENGER")
        
        void shouldUpdateLocationSuccessfully() throws Exception {
                EmployeeEntity messenger = new EmployeeEntity();
                messenger.setDocument(77766655L);
                messenger.setFullName("Messenger One");
                messenger.setRole(Role.MESSENGER);
                messenger.setPassword("secret123");
                messenger.setPhone("3111111111");
                entityManager.persist(messenger);
                entityManager.flush();

                LiveTrackingRequest request = new LiveTrackingRequest();
                request.setMessengerId(messenger.getIdEmployee());
                request.setLatitude(4.6789);
                request.setLongitude(-74.0567);
                request.setAccuracy(10.0);
                request.setSpeed(0.0);
                request.setHeading(0.0);
                request.setStatus(TrackingStatus.ACTIVE);
                request.setDeviceId("test-device");

                mockMvc.perform(post("/tracking/update")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.messengerId").value(messenger.getIdEmployee()))
                                .andExpect(jsonPath("$.latitude").value(4.6789))
                                .andExpect(jsonPath("$.longitude").value(-74.0567));
        }

        @Test
        @DisplayName("Debe obtener la última ubicación para el administrador")
@WithMockUser(roles = "ADMIN")
        
        void shouldGetLastLocationForAdmin() throws Exception {
                EmployeeEntity messenger = new EmployeeEntity();
                messenger.setDocument(55544433L);
                messenger.setFullName("Messenger Two");
                messenger.setRole(Role.MESSENGER);
                messenger.setPassword("secret123");
                messenger.setPhone("3222222222");
                entityManager.persist(messenger);
                entityManager.flush();

                LiveTrackingRequest request = new LiveTrackingRequest();
                request.setMessengerId(messenger.getIdEmployee());
                request.setLatitude(4.7110);
                request.setLongitude(-74.0720);
                request.setStatus(TrackingStatus.ACTIVE);

                app.domain.model.LiveTracking liveTracking = new app.domain.model.LiveTracking();
                liveTracking.setMessengerId(messenger.getIdEmployee());
                liveTracking.setCurrentLocation(
                                new app.domain.model.Location(4.7110, -74.0720, LocalDateTime.now(), 10.0));
                liveTracking.setStatus(TrackingStatus.ACTIVE);
                org.mockito.Mockito.when(trackingPort.getLastLocation(messenger.getIdEmployee()))
                                .thenReturn(java.util.Optional.of(liveTracking));

                mockMvc.perform(post("/tracking/update")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk());

                mockMvc.perform(get("/tracking/messenger/" + messenger.getUuid()))
                                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Debe retornar prohibido para no administradores")
@WithMockUser(roles = "MESSENGER")
        
        void shouldReturnForbiddenForNonAdmin() throws Exception {
                mockMvc.perform(get("/tracking/messenger/550e8400-e29b-41d4-a716-446655440000"))
                                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Debe obtener historial de rastreo")
@WithMockUser(roles = "ADMIN")
        
        void shouldGetTrackingHistory() throws Exception {
                EmployeeEntity messenger = new EmployeeEntity();
                messenger.setDocument(44455566L);
                messenger.setFullName("Messenger History");
                messenger.setRole(Role.MESSENGER);
                messenger.setPassword("secret123");
                messenger.setPhone("3444444444");
                entityManager.persist(messenger);
                entityManager.flush();

                String today = LocalDate.now().toString();

                org.mockito.Mockito.when(trackingPort.getHistoryByMessengerPaginated(
                                org.mockito.ArgumentMatchers.anyLong(),
                                org.mockito.ArgumentMatchers.any(),
                                org.mockito.ArgumentMatchers.any()))
                                .thenReturn(org.springframework.data.domain.Page.empty());

                mockMvc.perform(get("/tracking/history/pageable/" + messenger.getUuid())
                                .param("date", today)
                                .param("page", "0")
                                .param("size", "10"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content").exists());
        }

        @Test
        @DisplayName("Debe retornar solicitud incorrecta para coordenadas nulas")
@WithMockUser(roles = "MESSENGER")
        
        void shouldReturnBadRequestForNullCoordinates() throws Exception {
                LiveTrackingRequest request = new LiveTrackingRequest();
                request.setMessengerId(1L);
                request.setLatitude(null);
                request.setLongitude(null);

                mockMvc.perform(post("/tracking/update")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Debe obtener últimas ubicaciones masivas para el administrador")
@WithMockUser(roles = "ADMIN")
        
        void shouldGetBulkLastLocationsForAdmin() throws Exception {
                EmployeeEntity messenger1 = new EmployeeEntity();
                messenger1.setDocument(100100100L);
                messenger1.setFullName("Bulk Messenger 1");
                messenger1.setRole(Role.MESSENGER);
                messenger1.setPassword("secret123");
                messenger1.setPhone("3555555555");
                entityManager.persist(messenger1);

                EmployeeEntity messenger2 = new EmployeeEntity();
                messenger2.setDocument(200200200L);
                messenger2.setFullName("Bulk Messenger 2");
                messenger2.setRole(Role.MESSENGER);
                messenger2.setPassword("secret123");
                messenger2.setPhone("3666666666");
                entityManager.persist(messenger2);
                
                entityManager.flush();

                app.domain.model.LiveTracking liveTracking = new app.domain.model.LiveTracking();
                liveTracking.setMessengerId(messenger1.getIdEmployee());
                liveTracking.setCurrentLocation(new app.domain.model.Location(4.0, -74.0, LocalDateTime.now(), 10.0));
                liveTracking.setStatus(TrackingStatus.ACTIVE);
                
                org.mockito.Mockito.when(trackingPort.getLastLocation(messenger1.getIdEmployee()))
                                .thenReturn(java.util.Optional.of(liveTracking));
                org.mockito.Mockito.when(trackingPort.getLastLocation(messenger2.getIdEmployee()))
                                .thenReturn(java.util.Optional.empty());

                BulkLocationsRequest request = new BulkLocationsRequest();
                request.setUuids(Arrays.asList(messenger1.getUuid(), messenger2.getUuid()));

                mockMvc.perform(post("/tracking/messengers/bulk-locations")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$." + messenger1.getUuid()).exists())
                                .andExpect(jsonPath("$." + messenger2.getUuid()).doesNotExist());
        }
}
