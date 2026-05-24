package app.adapter.in.rest.controllers;

import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

import com.fasterxml.jackson.databind.ObjectMapper;

import app.adapter.in.rest.auth.ProfileRequest;
import app.domain.model.Employee;
import app.domain.model.enums.Role;
import app.domain.ports.EmployeePort;
import app.support.AbstractIntegrationTest;

@Transactional
@DisplayName("Pruebas unitarias de ProfileController Integration")
class ProfileControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Autowired
    private EmployeePort employeePort;

    @Autowired
    private ObjectMapper objectMapper;

    private Employee testAdmin;
    private Employee testMessenger;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        testAdmin = new Employee();
        testAdmin.setDocument(1234567890L);
        testAdmin.setFullName("Admin Test");
        testAdmin.setPhone("3001234567");
        testAdmin.setPassword("password123");
        testAdmin.setRole(Role.ADMIN);
        testAdmin = employeePort.save(testAdmin);

        testMessenger = new Employee();
        testMessenger.setDocument(9876543210L);
        testMessenger.setFullName("Messenger Test");
        testMessenger.setPhone("3119876543");
        testMessenger.setPassword("password123");
        testMessenger.setRole(Role.MESSENGER);
        testMessenger = employeePort.save(testMessenger);
    }

    @Nested
    @DisplayName("Debe retornar el perfil de administrador")
    class GetProfile {

        @Test
        @DisplayName("Debe retornar el perfil de administrador")
        @WithMockUser(username = "1234567890", roles = "ADMIN")
        void shouldReturnAdminProfile() throws Exception {
            mockMvc.perform(get("/profile/me"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.fullName", is("Admin Test")))
                    .andExpect(jsonPath("$.role", is("ADMIN")));
        }

        @Test
        @DisplayName("Debe retornar el perfil de mensajero")

        @WithMockUser(username = "9876543210", roles = "MESSENGER")
        void shouldReturnMessengerProfile() throws Exception {
            mockMvc.perform(get("/profile/me"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.fullName", is("Messenger Test")))
                    .andExpect(jsonPath("$.role", is("MESSENGER")));
        }

        @Test
        @DisplayName("Debe retornar 401 cuando no está autenticado")

        void shouldReturn401WhenNotAuthenticated() throws Exception {
            mockMvc.perform(get("/profile/me"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Debe actualizar el propio perfil")
    class UpdateProfile {

        @Test
        @DisplayName("Debe actualizar el propio perfil")
        @WithMockUser(username = "1234567890", roles = "ADMIN")
        void shouldUpdateOwnProfile() throws Exception {
            ProfileRequest request = new ProfileRequest();
            request.setFullName("Admin Updated");
            request.setPhone("3009998877");

            mockMvc.perform(put("/profile/me")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.fullName", is("Admin Updated")))
                    .andExpect(jsonPath("$.phone", is("3009998877")));

            Employee updated = employeePort.findById(testAdmin.getIdEmployee());
            assert updated.getFullName().equals("Admin Updated");
        }

        @Test
        @DisplayName("Debe rechazar datos inválidos")

        @WithMockUser(username = "1234567890", roles = "ADMIN")
        void shouldRejectInvalidData() throws Exception {
            ProfileRequest request = new ProfileRequest();
            request.setFullName("Ab"); // Too short
            request.setPhone("123");   // Invalid phone

            mockMvc.perform(put("/profile/me")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }
}
