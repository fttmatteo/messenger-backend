package app.application.usecase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import app.domain.model.auth.AuthCredentials;
import app.domain.model.auth.TokenResponse;
import app.domain.services.AuthenticationService;

/**
 * Caso de uso para autenticación de usuarios.
 * 
 * <p>
 * Procesa solicitudes de login delegando al servicio de autenticación del
 * dominio.
 * </p>
 */
@Component
public class LoginUseCase {
    @Autowired
    private AuthenticationService authenticationService;

    public TokenResponse login(AuthCredentials credentials) throws Exception {
        return authenticationService.authenticate(credentials);
    }
}