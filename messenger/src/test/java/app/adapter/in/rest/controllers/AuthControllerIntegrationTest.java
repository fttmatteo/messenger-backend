package app.adapter.in.rest.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import app.domain.model.auth.AuthCredentials;
import app.adapter.out.persistence.entities.EmployeeEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import app.support.AbstractIntegrationTest;
import app.infrastructure.security.TurnstileValidationService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@DisplayName("Pruebas unitarias de AuthController Integration")
@Transactional
class AuthControllerIntegrationTest extends AbstractIntegrationTest {

        @Autowired
        private WebApplicationContext context;

        @Autowired
        private PasswordEncoder passwordEncoder;

        @Autowired
        private EntityManager entityManager;

        @MockitoBean
        private TurnstileValidationService turnstileValidationService;

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
        @DisplayName("Debe iniciar sesión exitosamente")

        void shouldLoginSuccessfully() throws Exception {
                EmployeeEntity admin = new EmployeeEntity();
                admin.setDocument(12345678L);
                admin.setFullName("Admin User");
                admin.setPassword(passwordEncoder.encode("secret123"));
                admin.setRole(app.domain.model.enums.Role.ADMIN);
                entityManager.persist(admin);
                entityManager.flush();

                AuthCredentials credentials = new AuthCredentials();
                credentials.setDocument(12345678L);
                credentials.setPassword("secret123");
                credentials.setTurnstileToken("valid-token");

                when(turnstileValidationService.validateToken(anyString())).thenReturn(true);

                mockMvc.perform(post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(credentials)))
                                .andExpect(status().isOk())
                                .andExpect(cookie().exists("accessToken"))
                                .andExpect(cookie().exists("refreshToken"))
                                .andExpect(jsonPath("$.role", org.hamcrest.Matchers.is("ADMIN")))
                                .andExpect(jsonPath("$.message").exists());
        }

        @Test
        @DisplayName("Debe retornar no autorizado para contraseña inválida")

        void shouldReturnUnauthorizedForInvalidPassword() throws Exception {
                EmployeeEntity admin = new EmployeeEntity();
                admin.setDocument(87654321L);
                admin.setFullName("Admin User");
                admin.setPassword(passwordEncoder.encode("secret123"));
                admin.setRole(app.domain.model.enums.Role.ADMIN);
                entityManager.persist(admin);
                entityManager.flush();

                AuthCredentials credentials = new AuthCredentials();
                credentials.setDocument(87654321L);
                credentials.setPassword("wrongpassword");
                credentials.setTurnstileToken("valid-token");

                when(turnstileValidationService.validateToken(anyString())).thenReturn(true);
                mockMvc.perform(post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(credentials)))
                                .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("Debe refrescar el token exitosamente")

        void shouldRefreshTokenSuccessfully() throws Exception {
                EmployeeEntity admin = new EmployeeEntity();
                admin.setDocument(11112222L);
                admin.setFullName("Refresh User");
                admin.setPassword(passwordEncoder.encode("secret123"));
                admin.setRole(app.domain.model.enums.Role.ADMIN);
                entityManager.persist(admin);
                entityManager.flush();

                AuthCredentials credentials = new AuthCredentials();
                credentials.setDocument(11112222L);
                credentials.setPassword("secret123");
                credentials.setTurnstileToken("valid-token");

                when(turnstileValidationService.validateToken(anyString())).thenReturn(true);

                MvcResult loginResult = mockMvc.perform(post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(credentials)))
                                .andExpect(status().isOk())
                                .andExpect(cookie().exists("accessToken"))
                                .andExpect(cookie().exists("refreshToken"))
                                .andReturn();

                var refreshCookie = loginResult.getResponse().getCookie("refreshToken");
                org.junit.jupiter.api.Assertions.assertNotNull(refreshCookie,
                                "Refresh token cookie must not be null after login");

                mockMvc.perform(post("/auth/refresh")
                                .cookie(refreshCookie))
                                .andExpect(status().isOk())
                                .andExpect(cookie().exists("accessToken"))
                                .andExpect(jsonPath("$.message").exists());
        }

        @Test
        @DisplayName("Debe retornar solicitud incorrecta cuando el token Turnstile está vacío")

        void shouldReturnBadRequestWhenTurnstileTokenIsEmpty() throws Exception {
                AuthCredentials credentials = new AuthCredentials();
                credentials.setDocument(12345678L);
                credentials.setPassword("secret123");
                credentials.setTurnstileToken(""); // Empty token

                mockMvc.perform(post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(credentials)))
                                .andExpect(status().isBadRequest());
        }
}
