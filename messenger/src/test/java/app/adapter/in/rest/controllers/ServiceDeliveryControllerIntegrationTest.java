package app.adapter.in.rest.controllers;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import app.domain.model.enums.Status;
import app.infrastructure.persistence.entities.DealershipEntity;
import app.infrastructure.persistence.entities.EmployeeEntity;
import app.infrastructure.persistence.entities.PlateEntity;
import app.infrastructure.persistence.entities.ServiceDeliveryEntity;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.TimeZone;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("ServiceDeliveryController Integration Tests")
class ServiceDeliveryControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Autowired
    private EntityManager entityManager;

    @BeforeAll
    static void setupTimezone() {
        // Force timezone to match production setup
        TimeZone.setDefault(TimeZone.getTimeZone("America/Bogota"));
    }

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @DisplayName("GET /services/stats/daily should return 200 and correct JSON structure")
    @WithMockUser // Simulates an authenticated user
    /**
     * Verifica que el endpoint de estadísticas diarias retorne datos correctos y
     * completos.
     */
    void shouldReturnDailyStats() throws Exception {
        // Given: Prepare data in H2
        EmployeeEntity messenger = createEmployee("888888", "Integration Messenger");
        entityManager.persist(messenger);

        DealershipEntity dealership = createDealership("Integration Dealer");
        entityManager.persist(dealership);

        PlateEntity plate = createPlate("INT001");
        entityManager.persist(plate);

        // Use today's date at noon to avoid timezone edge cases
        LocalDateTime testDate = LocalDateTime.now().withHour(12).withMinute(0).withSecond(0).withNano(0);
        createAndPersistService(messenger, dealership, plate, Status.DELIVERED, testDate);

        entityManager.flush();

        // When/Then: Call API
        mockMvc.perform(get("/services/stats/daily")
                .param("messengerId", messenger.getIdEmployee().toString())
                .param("from", testDate.toLocalDate().toString())
                .param("to", testDate.toLocalDate().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].total", is(1)))
                .andExpect(jsonPath("$[0].delivered", is(1)))
                .andExpect(jsonPath("$[0].date", is(testDate.toLocalDate().toString())));
    }

    @Test
    @DisplayName("GET /services/stats/daily should return 401 if unauthenticated")
    /**
     * Verifica que se requiera autenticación para acceder a las estadísticas.
     */
    void shouldReturn401IfUnauthenticated() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        mockMvc.perform(get("/services/stats/daily")
                .param("messengerId", "1")
                .param("from", now.toLocalDate().toString())
                .param("to", now.toLocalDate().toString()))
                .andExpect(status().isUnauthorized()); // Or 403 depending on security config
    }

    private void createAndPersistService(EmployeeEntity messenger, DealershipEntity dealership, PlateEntity plate,
            Status status, LocalDateTime createdAt) {
        ServiceDeliveryEntity service = new ServiceDeliveryEntity();
        service.setMessenger(messenger);
        service.setDealership(dealership);
        service.setPlate(plate);
        service.setCurrentStatus(status);
        service.setCreatedAt(createdAt);
        service.setDeleted(false);
        entityManager.persist(service);
    }

    private EmployeeEntity createEmployee(String document, String name) {
        EmployeeEntity e = new EmployeeEntity();
        e.setDocument(Long.parseLong(document));
        e.setFullName(name);
        e.setPassword("pass");
        e.setRole(app.domain.model.enums.Role.MESSENGER);
        return e;
    }

    private DealershipEntity createDealership(String name) {
        DealershipEntity d = new DealershipEntity();
        d.setName(name);
        d.setAddress("Address");
        d.setPhone("1234567890");
        d.setZone("Zone");
        return d;
    }

    private PlateEntity createPlate(String number) {
        PlateEntity p = new PlateEntity();
        p.setPlateNumber(number);
        p.setPlateType(app.domain.model.enums.PlateType.CAR);
        return p;
    }
}
