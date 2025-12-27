package app.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import app.domain.model.auth.AuthCredentials;
import app.domain.model.auth.TokenResponse;
import app.domain.services.AuthenticationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoginUseCase Unit Tests")
class LoginUseCaseTest {

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private LoginUseCase loginUseCase;

    @Test
    @DisplayName("Debe delegar login al servicio de autenticación")
    /**
     * Verifica que el caso de uso llame correctamente al servicio de autenticación.
     */
    void shouldDelegateLogin() throws Exception {
        AuthCredentials credentials = new AuthCredentials();
        credentials.setDocument(123456789L);
        TokenResponse expectedResponse = new TokenResponse();
        expectedResponse.setToken("token");
        expectedResponse.setRefreshToken("refresh");

        when(authenticationService.authenticate(credentials)).thenReturn(expectedResponse);

        TokenResponse actualResponse = loginUseCase.login(credentials);

        assertEquals(expectedResponse, actualResponse);
        verify(authenticationService).authenticate(credentials);
    }

    @Test
    @DisplayName("Debe propagar excepciones del servicio")
    void shouldPropagateExceptions() throws Exception {
        AuthCredentials credentials = new AuthCredentials();
        when(authenticationService.authenticate(credentials)).thenThrow(new RuntimeException("Error"));

        assertThrows(RuntimeException.class, () -> loginUseCase.login(credentials));
    }
}
