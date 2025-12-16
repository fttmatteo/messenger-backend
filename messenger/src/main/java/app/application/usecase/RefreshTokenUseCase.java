package app.application.usecase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import app.domain.model.auth.RefreshTokenRequest;
import app.domain.model.auth.TokenResponse;
import app.domain.services.AuthenticationService;

/**
 * Caso de uso para la renovación de tokens JWT (Refresh Token Flow).
 * 
 * Orquesta la lógica de negocio para validar un refresh token existente
 * y generar un nuevo par de tokens (access token + refresh token),
 * delegando la operación al servicio de autenticación.
 */
@Component
public class RefreshTokenUseCase {

    private static final Logger logger = LoggerFactory.getLogger(RefreshTokenUseCase.class);

    @Autowired
    private AuthenticationService authenticationService;

    /**
     * Procesa la solicitud de refresco de token.
     * 
     * @param request Solicitud con el refresh token.
     * @return Nuevos tokens (access y refresh).
     * @throws Exception Si el token es inválido.
     */
    public TokenResponse refreshToken(RefreshTokenRequest request) throws Exception {
        logger.debug("Procesando solicitud de refresh token");
        return authenticationService.refreshToken(request.getRefreshToken());
    }
}
