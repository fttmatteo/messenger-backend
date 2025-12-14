package app.adapter.in.rest.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import app.application.exceptions.BusinessException;
import app.application.exceptions.InputsException;
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

    @Autowired
    private LoginUseCase loginUseCase;

    /**
     * Inicia sesión con las credenciales proporcionadas.
     *
     * @param credentials Objeto que contiene el nombre de usuario y contraseña.
     * @return ResponseEntity con la respuesta del token (TokenResponse) si es
     *         exitoso,
     *         o mensajes de error en caso de fallo (400, 401, 500).
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthCredentials credentials) {
        try {
            TokenResponse response = loginUseCase.login(credentials);
            return ResponseEntity.ok(response);
        } catch (InputsException ie) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ie.getMessage());
        } catch (BusinessException be) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(be.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}