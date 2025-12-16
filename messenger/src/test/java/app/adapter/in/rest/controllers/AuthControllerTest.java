package app.adapter.in.rest.controllers;

import app.application.usecase.LoginUseCase;
import app.application.usecase.RefreshTokenUseCase;
import app.domain.model.auth.AuthCredentials;
import app.domain.model.auth.TokenResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false) // Deshabilitar filtros de seguridad para probar solo el controlador
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LoginUseCase loginUseCase;

    @MockitoBean
    private RefreshTokenUseCase refreshTokenUseCase;

    @MockitoBean
    private app.domain.ports.AuthenticationPort authenticationPort;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("POST /auth/login - Debería retornar TokenResponse cuando las credenciales son válidas")
    void loginShouldReturnToken() throws Exception {
        // Arrange
        AuthCredentials credentials = new AuthCredentials();
        credentials.setUserName("testuser");
        credentials.setPassword("password123");

        TokenResponse mockResponse = new TokenResponse();
        mockResponse.setToken("mock-jwt-token");
        mockResponse.setRefreshToken("mock-refresh-token");
        mockResponse.setRole("ADMIN");

        when(loginUseCase.login(any(AuthCredentials.class))).thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(credentials)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-jwt-token"))
                .andExpect(jsonPath("$.refreshToken").value("mock-refresh-token"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }
}
