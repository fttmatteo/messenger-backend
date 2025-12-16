package app.application.usecase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import app.domain.model.auth.AuthCredentials;
import app.domain.model.auth.TokenResponse;
import app.domain.services.AuthenticationService;

/**
 * Caso de uso para autenticación de usuarios.
 * 
 * Procesa solicitudes de login delegando al servicio de autenticación del
 * dominio.
 */
@Component
public class LoginUseCase {

    private static final Logger logger = LoggerFactory.getLogger(LoginUseCase.class);

    @Autowired
    private AuthenticationService authenticationService;

    /**
     * Procesa la solicitud de inicio de sesión.
     * 
     * @param credentials Las credenciales de autenticación (usuario y contraseña).
     * @return La respuesta con el token de acceso si las credenciales son válidas.
     * @throws Exception Si las credenciales son inválidas o hay un error de
     *                   autenticación.
     */
    public TokenResponse login(AuthCredentials credentials) throws Exception {
        logger.debug("Procesando solicitud de login para: {}", credentials.getUserName());
        TokenResponse response = authenticationService.authenticate(credentials);
        logger.info("Login procesado exitosamente para: {}", credentials.getUserName());
        return response;
    }
}