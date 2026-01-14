package app.adapter.in.rest.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import app.domain.model.enums.Role;
import app.infrastructure.persistence.entities.DealershipEntity;
import app.infrastructure.persistence.entities.EmployeeEntity;
import app.infrastructure.persistence.entities.PlateEntity;
import app.domain.model.enums.PlateType;
import app.infrastructure.persistence.entities.ServiceDeliveryEntity;
import app.infrastructure.persistence.entities.StatusHistoryEntity;
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
@DisplayName("MonitoringController Integration Tests")
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
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /monitoring/messenger/{id}/activity should return summary and timeline")
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

        PlateEntity plate = new PlateEntity();
        plate.setPlateNumber("MON123");
        plate.setPlateType(PlateType.CAR);
        entityManager.persist(plate);

        ServiceDeliveryEntity service = new ServiceDeliveryEntity();
        service.setMessenger(messenger);
        service.setDealership(dealership);
        service.setPlate(plate);
        service.setCurrentStatus(Status.DELIVERED);
        service.setCreatedAt(LocalDateTime.now());
        entityManager.persist(service);

        StatusHistoryEntity history = new StatusHistoryEntity();
        history.setServiceDelivery(service);
        history.setNewStatus(Status.DELIVERED);
        history.setChangeDate(LocalDateTime.now());
        history.setChangedBy(messenger);
        history.setDeliveryLatitude(4.0);
        history.setDeliveryLongitude(-74.0);
        entityManager.persist(history);

        service.getHistory().add(history);
        entityManager.merge(service);

        entityManager.flush();

        String today = LocalDate.now().toString();

        mockMvc.perform(get("/monitoring/messenger/" + messenger.getIdEmployee() + "/activity")
                .param("date", today))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dailyStats.total").value(1))
                .andExpect(jsonPath("$.dailyStats.delivered").value(1))
                .andExpect(jsonPath("$.timeline[0].status").value("DELIVERED"));
    }

    @Test
    @WithMockUser(roles = "MESSENGER")
    @DisplayName("GET /monitoring/messenger/{id}/activity should return 403 for messenger role")
    void shouldReturnForbiddenForMessenger() throws Exception {
        mockMvc.perform(get("/monitoring/messenger/1/activity")
                .param("date", "2024-01-05"))
                .andExpect(status().isForbidden());
    }
}
