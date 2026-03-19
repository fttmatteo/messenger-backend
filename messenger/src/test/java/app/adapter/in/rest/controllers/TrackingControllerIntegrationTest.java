package app.adapter.in.rest.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import app.adapter.in.rest.request.LiveTrackingRequest;
import app.domain.model.enums.Role;
import app.domain.model.enums.TrackingStatus;
import app.infrastructure.persistence.entities.EmployeeEntity;
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
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import app.support.AbstractIntegrationTest;

@Transactional
@DisplayName("TrackingController Integration Tests")
/**
 * Clase de pruebas integración para el controlador de seguimiento.
 */
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
        @WithMockUser(roles = "MESSENGER")
        @DisplayName("POST /tracking/update should return 200 and save location")
        /**
         * Verifica que el endpoint de actualización de ubicación retorne 200.
         */
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
        @WithMockUser(roles = "ADMIN")
        @DisplayName("GET /tracking/messenger/{id} should return location for admin")
        /**
         * Verifica que el endpoint de ubicación retorne 200.
         */
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
        @WithMockUser(roles = "MESSENGER")
        @DisplayName("GET /tracking/messenger/{id} should return 403 for non-admin")
        /**
         * Verifica que el endpoint de ubicación retorne 403.
         */
        void shouldReturnForbiddenForNonAdmin() throws Exception {
                mockMvc.perform(get("/tracking/messenger/550e8400-e29b-41d4-a716-446655440000"))
                                .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("GET /tracking/history/{id} should return historical data")
        /**
         * Verifica que el endpoint de historial retorne 200.
         */
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

                mockMvc.perform(get("/tracking/history/" + messenger.getUuid())
                                .param("date", today))
                                .andExpect(status().isOk());
        }
}
