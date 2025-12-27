package app.application.usecase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import app.domain.model.auth.RefreshTokenRequest;
import app.domain.model.auth.TokenResponse;
import app.domain.services.AuthenticationService;

/**
 * Caso de uso para renovar tokens JWT.
 */
@Component
public class RefreshTokenUseCase {

    @Autowired
    private AuthenticationService authenticationService;

    /**
     * Renueva un token de acceso utilizando un refresh token válido.
     */
    public TokenResponse refreshToken(RefreshTokenRequest request) throws Exception {
        return authenticationService.refreshToken(request.getRefreshToken());
    }
}
