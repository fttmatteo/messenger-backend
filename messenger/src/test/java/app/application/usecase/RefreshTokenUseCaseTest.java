package app.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import app.domain.model.auth.RefreshTokenRequest;
import app.domain.model.auth.TokenResponse;
import app.domain.services.AuthenticationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas unitarias de RefreshTokenUseCase")
class RefreshTokenUseCaseTest {

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private RefreshTokenUseCase refreshTokenUseCase;

    @Test
    @DisplayName("Debe delegar refresh al servicio")
    void shouldDelegateRefresh() throws Exception {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("oldRefresh");
        TokenResponse expectedResponse = new TokenResponse();
        expectedResponse.setToken("newAccess");
        expectedResponse.setRefreshToken("newRefresh");

        when(authenticationService.refreshToken("oldRefresh")).thenReturn(expectedResponse);

        TokenResponse actualResponse = refreshTokenUseCase.refreshToken(request);

        assertEquals(expectedResponse, actualResponse);
        verify(authenticationService).refreshToken("oldRefresh");
    }

    @Test
    @DisplayName("Debe propagar excepciones")

    void shouldPropagateExceptions() throws Exception {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("bad");
        when(authenticationService.refreshToken("bad")).thenThrow(new RuntimeException("Invalid"));

        assertThrows(RuntimeException.class, () -> refreshTokenUseCase.refreshToken(request));
    }
}
