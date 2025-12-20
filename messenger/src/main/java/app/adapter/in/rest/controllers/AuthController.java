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

/**
 * Controlador REST para autenticación y gestión de tokens.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private RefreshTokenUseCase refreshTokenUseCase;
    @Autowired
    private LoginUseCase loginUseCase;

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody AuthCredentials credentials) throws Exception {
        TokenResponse response = loginUseCase.login(credentials);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody app.domain.model.auth.RefreshTokenRequest request)
            throws Exception {
        TokenResponse response = refreshTokenUseCase.refreshToken(request);
        return ResponseEntity.ok(response);
    }
}