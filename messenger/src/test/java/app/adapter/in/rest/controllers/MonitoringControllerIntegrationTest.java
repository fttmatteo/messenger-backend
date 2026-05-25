package app.adapter.in.rest.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import app.domain.model.enums.Role;
import app.adapter.out.persistence.entities.DealershipEntity;
import app.adapter.out.persistence.entities.EmployeeEntity;
import app.adapter.out.persistence.entities.PlateEntity;
import app.domain.model.enums.PlateType;
import app.adapter.out.persistence.entities.ServiceDeliveryEntity;
import app.domain.model.enums.Status;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import java.time.LocalDate;
import java.time.LocalDateTime;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import app.support.AbstractIntegrationTest;

@Transactional
@DisplayName("Pruebas unitarias de MonitoringController Integration")
class MonitoringControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private EntityManager entityManager;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @DisplayName("Debe retornar resumen de actividad del mensajero")
@WithMockUser(roles = "ADMIN")
    
    void shouldReturnMessengerActivitySummary() throws Exception {
        EmployeeEntity messenger = new EmployeeEntity();
        messenger.setDocument(99998888L);
        messenger.setFullName("Test Messenger");
        messenger.setRole(Role.MESSENGER);
        messenger.setPassword("secret123");
        messenger.setPhone("3000000000");
        entityManager.persist(messenger);

        DealershipEntity dealership = new DealershipEntity();
        dealership.setName("Dealership Monitoring Test");
        dealership.setAddress("Calle 100 # 15-20, Bogotá");
        dealership.setPhone("3001234567");
        dealership.setZone("Norte");
        entityManager.persist(dealership);

        DealershipEntity originDealership = new DealershipEntity();
        originDealership.setName("Origin Monitoring Test");
        originDealership.setAddress("Calle 200 # 30-40, Bogotá");
        originDealership.setPhone("3009876543");
        originDealership.setZone("Sur");
        entityManager.persist(originDealership);

        PlateEntity plate = new PlateEntity();
        plate.setPlateNumber("MON123");
        plate.setPlateType(PlateType.MOTORCYCLE);
        entityManager.persist(plate);

        ServiceDeliveryEntity service = new ServiceDeliveryEntity();
        service.setMessenger(messenger);
        service.setDealership(dealership);
        service.setOriginDealership(originDealership);
        service.setPlate(plate);
        service.setCurrentStatus(Status.DELIVERED);
        service.setCreatedAt(LocalDateTime.now());
        entityManager.persist(service);

        app.adapter.out.persistence.entities.TimelineEventEntity timelineEvent = new app.adapter.out.persistence.entities.TimelineEventEntity();
        timelineEvent.setMessengerId(messenger.getIdEmployee());
        timelineEvent.setEventDate(LocalDate.now());
        timelineEvent.setTimestamp(LocalDateTime.now());
        timelineEvent.setStatus(Status.DELIVERED.name());
        timelineEvent.setPlateNumber("MON123");
        timelineEvent.setDealershipName("Dealership Monitoring Test");
        timelineEvent.setChangedByName("Test Messenger");
        timelineEvent.setChangedByRole("MESSENGER");
        entityManager.persist(timelineEvent);

        entityManager.flush();

        String today = LocalDate.now().toString();

        mockMvc.perform(get("/monitoring/messenger/" + messenger.getUuid() + "/activity")
                .param("date", today))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.timeline").isArray())
                .andExpect(jsonPath("$.timeline[0].status").value("DELIVERED"));
    }

    @Test
    @DisplayName("Debe retornar prohibido para el mensajero")
@WithMockUser(roles = "MESSENGER")
    
    void shouldReturnForbiddenForMessenger() throws Exception {
        mockMvc.perform(get("/monitoring/messenger/550e8400-e29b-41d4-a716-446655440000/activity")
                .param("date", "2024-01-05"))
                .andExpect(status().isForbidden());
    }
}
