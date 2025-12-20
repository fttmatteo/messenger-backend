package app.application.usecase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import app.domain.model.auth.AuthCredentials;
import app.domain.model.auth.TokenResponse;
import app.domain.services.AuthenticationService;

/**
 * Caso de uso para login de usuarios.
 */
@Component
public class LoginUseCase {

    private static final Logger logger = LoggerFactory.getLogger(LoginUseCase.class);

    @Autowired
    private AuthenticationService authenticationService;

    public TokenResponse login(AuthCredentials credentials) throws Exception {
        logger.info("Intento de login para documento: {}", credentials.getDocument());
        try {
            TokenResponse response = authenticationService.authenticate(credentials);
            logger.info("Login exitoso para documento: {}", credentials.getDocument());
            return response;
        } catch (Exception e) {
            logger.warn("Login fallido para documento: {} - Razón: {}", credentials.getDocument(), e.getMessage());
            throw e;
        }
    }
}