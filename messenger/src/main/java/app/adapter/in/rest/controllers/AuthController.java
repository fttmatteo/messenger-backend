package app.adapter.in.rest.controllers;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import app.application.usecase.LoginUseCase;
import app.domain.model.auth.AuthCredentials;
import app.domain.model.auth.TokenResponse;

/**
 * Controlador REST para el manejo de la autenticación de usuarios.
 * Proporciona endpoints para iniciar sesión y obtener tokens JWT.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private LoginUseCase loginUseCase;

    /**
     * Inicia sesión con las credenciales proporcionadas.
     *
     * @param credentials Objeto que contiene el nombre de usuario y contraseña.
     * @return ResponseEntity con la respuesta del token (TokenResponse) si es
     *         exitoso.
     * @throws Exception Si las credenciales son inválidas o hay un error de
     *                   autenticación.
     */
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody AuthCredentials credentials) throws Exception {
        logger.info("Intento de login para usuario: {}", credentials.getUserName());
        TokenResponse response = loginUseCase.login(credentials);
        logger.info("Login exitoso para usuario: {} con rol: {}", credentials.getUserName(), response.getRole());
        return ResponseEntity.ok(response);
    }
}