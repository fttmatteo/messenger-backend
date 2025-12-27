package app.adapter.in.rest.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import app.application.usecase.LoginUseCase;
import app.application.usecase.RefreshTokenUseCase;
import app.domain.model.auth.AuthCredentials;
import app.domain.model.auth.TokenResponse;
import app.infrastructure.audit.AuditableAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controlador REST para autenticación y gestión de tokens.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private RefreshTokenUseCase refreshTokenUseCase;
    @Autowired
    private LoginUseCase loginUseCase;

    /**
     * Inicia sesión de usuario y genera tokens de acceso.
     */
    @PostMapping("/login")
    @AuditableAction(action = "LOGIN", description = "Inicio de sesión de usuario")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody AuthCredentials credentials) throws Exception {
        logger.info("Solicitud de login recibida para documento: {}", credentials.getDocument());
        TokenResponse response = loginUseCase.login(credentials);
        return ResponseEntity.ok(response);
    }

    /**
     * Renueva el token de acceso utilizando un refresh token válido.
     */
    @PostMapping("/refresh")
    @AuditableAction(action = "TOKEN_REFRESH", description = "Renovación de token de acceso")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody app.domain.model.auth.RefreshTokenRequest request)
            throws Exception {
        TokenResponse response = refreshTokenUseCase.refreshToken(request);
        return ResponseEntity.ok(response);
    }
}