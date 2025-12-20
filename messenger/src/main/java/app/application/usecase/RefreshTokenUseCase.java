package app.application.usecase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import app.domain.model.auth.RefreshTokenRequest;
import app.domain.model.auth.TokenResponse;
import app.domain.services.AuthenticationService;

@Component
public class RefreshTokenUseCase {

    @Autowired
    private AuthenticationService authenticationService;

    public TokenResponse refreshToken(RefreshTokenRequest request) throws Exception {
        return authenticationService.refreshToken(request.getRefreshToken());
    }
}
