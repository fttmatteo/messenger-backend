package app.adapter.in.rest.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import app.domain.model.auth.AuthCredentials;
import app.infrastructure.persistence.entities.EmployeeEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("AuthController Integration Tests")
class AuthControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
    @DisplayName("POST /auth/login should return 200 and tokens for valid credentials")
    /**
     * Verifica que el endpoint de login retorne tokens (access y refresh) cuando
     * las credenciales son correctas.
     */
    void shouldLoginSuccessfully() throws Exception {
        // Given
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

        // When/Then
        mockMvc.perform(post("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(credentials)))
            .andExpect(status().isOk())
            // Tokens ahora van en cookies HttpOnly
            .andExpect(cookie().exists("accessToken"))
            .andExpect(cookie().exists("refreshToken"))
            .andExpect(jsonPath("$.role", org.hamcrest.Matchers.is("ADMIN")))
            .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("POST /auth/login should return 400 or 401 for invalid password")
    /**
     * Verifica que el endpoint rechace el login con contraseña incorrecta.
     */
    void shouldReturnUnauthorizedForInvalidPassword() throws Exception {
        // Given
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

        // When/Then (Note: Depending on global exception handler, it might be 401 or
        // 400/500 if BusinessException)
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(credentials)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("POST /auth/refresh should return new tokens for valid refresh token")
    /**
     * Verifica la rotación de tokens mediante el refreshToken.
     */
    void shouldRefreshTokenSuccessfully() throws Exception {
        // Given: Create user and get refresh token
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

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(credentials)))
            .andReturn();

        // Obtener refresh token desde cookie
        var refreshCookie = loginResult.getResponse().getCookie("refreshToken");

        // When/Then: usar la cookie en la solicitud de refresh (sin body)
        mockMvc.perform(post("/auth/refresh")
            .cookie(refreshCookie))
            .andExpect(status().isOk())
            .andExpect(cookie().exists("accessToken"))
            // Puede que renueve refreshToken o no; validamos si existe
            .andExpect(jsonPath("$.message").exists());
    }
}
