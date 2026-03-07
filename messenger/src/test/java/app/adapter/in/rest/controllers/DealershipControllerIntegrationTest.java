package app.adapter.in.rest.controllers;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import app.adapter.in.rest.request.DealershipRequest;
import app.infrastructure.persistence.entities.DealershipEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import app.support.AbstractIntegrationTest;

@Transactional
@DisplayName("DealershipController Integration Tests")
/**
 * Clase de pruebas integración para el controlador de concesionarios.
 */
class DealershipControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private EntityManager entityManager;

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
    @DisplayName("GET /dealerships/allDealerships should return 200 for authenticated user")
    @WithMockUser(roles = "MESSENGER")
    /**
     * Verifica que un usuario autenticado pueda listar condesionarios.
     */
    void shouldReturnAllDealerships() throws Exception {
        DealershipEntity d = new DealershipEntity();
        d.setName("Test Dealer");
        d.setAddress("Calle 123");
        d.setPhone("3001234567");
        d.setZone("Norte");
        entityManager.persist(d);
        entityManager.flush();

        mockMvc.perform(get("/dealerships/allDealerships"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(org.hamcrest.Matchers.greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].name", is("Test Dealer")));
    }

    @Test
    @DisplayName("POST /dealerships/createDealership should return 403 for MESSENGER")
    @WithMockUser(roles = "MESSENGER")
    /**
     * Verifica que un rol MESSENGER no tenga permiso para crear concesionarios.
     */
    void shouldDenyCreateForMessenger() throws Exception {
        DealershipRequest request = new DealershipRequest();
        request.setName("Forbidden Dealer");

        mockMvc.perform(post("/dealerships/createDealership")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /dealerships/createDealership should return 201 for ADMIN")
    @WithMockUser(roles = "ADMIN")
    /**
     * Verifica que un rol ADMIN pueda crear concesionarios exitosamente.
     */
    void shouldCreateDealershipForAdmin() throws Exception {
        DealershipRequest request = new DealershipRequest();
        request.setName("New Admin Dealer");
        request.setAddress("Av. Siempre Viva");
        request.setPhone("3001234567");
        request.setZone("Norte");

        mockMvc.perform(post("/dealerships/createDealership")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("New Admin Dealer")));
    }

    @Test
    @DisplayName("DELETE /dealerships/deleteDealership/{id} should delete dealership if ADMIN")
    @WithMockUser(roles = "ADMIN")
    /**
     * Verifica que un rol ADMIN pueda eliminar concesionarios.
     */
    void shouldDeleteDealership() throws Exception {
        DealershipEntity d = new DealershipEntity();
        d.setName("Dealer to Delete");
        d.setAddress("Street");
        d.setPhone("000");
        d.setZone("Z");
        entityManager.persist(d);
        entityManager.flush();

        mockMvc.perform(delete("/dealerships/deleteDealership/" + d.getIdDealership())
                .with(csrf()))
                .andExpect(status().isNoContent());

        DealershipEntity deleted = entityManager.find(DealershipEntity.class, d.getIdDealership());
        org.junit.jupiter.api.Assertions.assertNull(deleted);
    }
}
