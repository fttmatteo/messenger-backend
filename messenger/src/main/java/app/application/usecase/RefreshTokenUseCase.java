package app.application.usecase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(RefreshTokenUseCase.class);

    @Autowired
    private AuthenticationService authenticationService;

    /**
     * Renueva un token de acceso utilizando un refresh token válido.
     */
    public TokenResponse refreshToken(RefreshTokenRequest request) {
        try {
            return authenticationService.refreshToken(request.getRefreshToken());
        } catch (app.domain.exception.BusinessException | app.domain.exception.UnauthorizedException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error durante renovación de token: {}", e.getMessage(), e);
            throw new app.domain.exception.BusinessException("Error durante renovación de token: " + e.getMessage());
        }
    }
}
