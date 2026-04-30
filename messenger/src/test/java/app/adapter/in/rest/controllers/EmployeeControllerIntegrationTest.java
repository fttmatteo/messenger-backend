package app.adapter.in.rest.controllers;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import app.adapter.in.rest.request.EmployeeRequest;
import app.domain.model.enums.Role;
import app.infrastructure.persistence.entities.EmployeeEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
@DisplayName("EmployeeController Integration Tests")
class EmployeeControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private EntityManager entityManager;

    private ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

    private static final String ADMIN_DOCUMENT = "111111";

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        EmployeeEntity admin = new EmployeeEntity();
        admin.setDocument(Long.parseLong(ADMIN_DOCUMENT));
        admin.setFullName("Admin Test");
        admin.setRole(Role.ADMIN);
        admin.setPassword("pass");
        admin.setPhone("3000000000");
        entityManager.persist(admin);
        entityManager.flush();
    }

    @Nested
    @DisplayName("GET /employees/allEmployees")
    class FindAll {

        @Test
        @DisplayName("should return only MESSENGER employees for ADMIN (no other admins)")
        @WithMockUser(username = ADMIN_DOCUMENT, roles = "ADMIN")
        void shouldReturnOnlyMessengersForAdmin() throws Exception {
            EmployeeEntity messenger = new EmployeeEntity();
            messenger.setDocument(999L);
            messenger.setFullName("Test Messenger");
            messenger.setRole(Role.MESSENGER);
            messenger.setPassword("pass");
            messenger.setPhone("3001111111");
            entityManager.persist(messenger);

            EmployeeEntity otherAdmin = new EmployeeEntity();
            otherAdmin.setDocument(888L);
            otherAdmin.setFullName("Other Admin");
            otherAdmin.setRole(Role.ADMIN);
            otherAdmin.setPassword("pass");
            otherAdmin.setPhone("3002222222");
            entityManager.persist(otherAdmin);
            entityManager.flush();

            mockMvc.perform(get("/employees/allEmployees"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].fullName", is("Test Messenger")))
                    .andExpect(jsonPath("$[0].role", is("MESSENGER")));
        }

        @Test
        @DisplayName("should return 403 for MESSENGER role")
        @WithMockUser(roles = "MESSENGER")
        void shouldDenyAccessToMessenger() throws Exception {
            mockMvc.perform(get("/employees/allEmployees"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("POST /employees/createEmployee")
    class Create {

        @Test
        @DisplayName("should create MESSENGER employee")
        @WithMockUser(username = ADMIN_DOCUMENT, roles = "ADMIN")
        void shouldCreateMessengerEmployee() throws Exception {
            EmployeeRequest request = new EmployeeRequest();
            request.setDocument("101010");
            request.setFullName("New Messenger");
            request.setPhone("3001234567");
            request.setPassword("Secure@123");
            request.setRole("MESSENGER");

            mockMvc.perform(post("/employees/createEmployee")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.fullName", is("New Messenger")))
                    .andExpect(jsonPath("$.role", is("MESSENGER")));
        }

        @Test
        @DisplayName("should reject creation of ADMIN employees")
        @WithMockUser(username = ADMIN_DOCUMENT, roles = "ADMIN")
        void shouldRejectAdminCreation() throws Exception {
            EmployeeRequest request = new EmployeeRequest();
            request.setDocument("303030");
            request.setFullName("New Admin Attempt");
            request.setPhone("3009876543");
            request.setPassword("Secure@123");
            request.setRole("ADMIN");

            mockMvc.perform(post("/employees/createEmployee")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("DELETE /employees/deleteEmployee")
    class Delete {

        @Test
        @DisplayName("should delete MESSENGER employee")
        @WithMockUser(username = ADMIN_DOCUMENT, roles = "ADMIN")
        void shouldDeleteMessengerEmployee() throws Exception {
            EmployeeEntity emp = new EmployeeEntity();
            emp.setDocument(202020L);
            emp.setFullName("Delete Me");
            emp.setRole(Role.MESSENGER);
            emp.setPassword("pass");
            emp.setPhone("3003333333");
            entityManager.persist(emp);
            entityManager.flush();

            mockMvc.perform(delete("/employees/deleteEmployee/" + emp.getUuid())
                    .with(csrf()))
                    .andExpect(status().isNoContent());

            EmployeeEntity deleted = entityManager.find(EmployeeEntity.class, emp.getIdEmployee());
            org.junit.jupiter.api.Assertions.assertNull(deleted);
        }

        @Test
        @DisplayName("should reject deletion of another ADMIN")
        @WithMockUser(username = ADMIN_DOCUMENT, roles = "ADMIN")
        void shouldRejectDeletionOfOtherAdmin() throws Exception {
            EmployeeEntity otherAdmin = new EmployeeEntity();
            otherAdmin.setDocument(404040L);
            otherAdmin.setFullName("Other Admin");
            otherAdmin.setRole(Role.ADMIN);
            otherAdmin.setPassword("pass");
            otherAdmin.setPhone("3004444444");
            entityManager.persist(otherAdmin);
            entityManager.flush();

            mockMvc.perform(delete("/employees/deleteEmployee/" + otherAdmin.getUuid())
                    .with(csrf()))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("should reject self-deletion")
        @WithMockUser(username = ADMIN_DOCUMENT, roles = "ADMIN")
        void shouldRejectSelfDeletion() throws Exception {
            EmployeeEntity selfAdmin = entityManager
                    .createQuery("SELECT e FROM EmployeeEntity e WHERE e.document = :doc", EmployeeEntity.class)
                    .setParameter("doc", Long.parseLong(ADMIN_DOCUMENT))
                    .getSingleResult();

            mockMvc.perform(delete("/employees/deleteEmployee/" + selfAdmin.getUuid())
                    .with(csrf()))
                    .andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("GET /employees/findByEmployeeId")
    class FindByUuid {

        @Test
        @DisplayName("should return 404 when accessing another admin's profile")
        @WithMockUser(username = ADMIN_DOCUMENT, roles = "ADMIN")
        void shouldReturn404ForOtherAdminProfile() throws Exception {
            EmployeeEntity otherAdmin = new EmployeeEntity();
            otherAdmin.setDocument(505050L);
            otherAdmin.setFullName("Hidden Admin");
            otherAdmin.setRole(Role.ADMIN);
            otherAdmin.setPassword("pass");
            otherAdmin.setPhone("3005555555");
            entityManager.persist(otherAdmin);
            entityManager.flush();

            mockMvc.perform(get("/employees/findByEmployeeId/" + otherAdmin.getUuid()))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("should allow access to a MESSENGER profile")
        @WithMockUser(username = ADMIN_DOCUMENT, roles = "ADMIN")
        void shouldAllowAccessToMessengerProfile() throws Exception {
            EmployeeEntity messenger = new EmployeeEntity();
            messenger.setDocument(606060L);
            messenger.setFullName("Visible Messenger");
            messenger.setRole(Role.MESSENGER);
            messenger.setPassword("pass");
            messenger.setPhone("3006666666");
            entityManager.persist(messenger);
            entityManager.flush();

            mockMvc.perform(get("/employees/findByEmployeeId/" + messenger.getUuid()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.fullName", is("Visible Messenger")));
        }
    }
}

