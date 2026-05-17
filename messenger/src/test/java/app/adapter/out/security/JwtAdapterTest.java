package app.adapter.out.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import app.domain.model.auth.AuthCredentials;
import app.domain.model.auth.TokenResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Pruebas unitarias de JwtAdapter")
class JwtAdapterTest {

    private JwtAdapter jwtAdapter;

    @BeforeEach
    void setUp() {
        jwtAdapter = new JwtAdapter("VGVzdFNlY3JldEtleUZvckpXVFRlc3RpbmdQdXJwb3Nlc09ubHlNdXN0QmU", 1800000L);
    }

    @Test
    @DisplayName("Debe autenticar y generar un token")
    void shouldAuthenticateAndGenerateToken() {
        AuthCredentials credentials = new AuthCredentials();
        credentials.setDocument(123456789L);

        TokenResponse response = jwtAdapter.authenticate(credentials, "ADMIN", 1L);

        assertNotNull(response);
        assertNotNull(response.getToken());
        assertEquals("ADMIN", response.getRole());
    }

    @Test
    @DisplayName("Debe validar un token válido")

    void shouldValidateValidToken() {
        AuthCredentials credentials = new AuthCredentials();
        credentials.setDocument(123456789L);

        TokenResponse response = jwtAdapter.authenticate(credentials, "ADMIN", 1L);
        boolean isValid = jwtAdapter.validateToken(response.getToken());

        assertTrue(isValid);
    }

    @Test
    @DisplayName("Debe rechazar un token inválido")

    void shouldRejectInvalidToken() {
        boolean isValid = jwtAdapter.validateToken("invalid.token.here");

        assertFalse(isValid);
    }

    @Test
    @DisplayName("Debe extraer el documento del token")
    void shouldExtractDocumentFromToken() {
        AuthCredentials credentials = new AuthCredentials();
        credentials.setDocument(123456789L);

        TokenResponse response = jwtAdapter.authenticate(credentials, "ADMIN", 1L);
        String document = jwtAdapter.extractUsername(response.getToken());

        assertEquals("123456789", document);
    }

    @Test
    @DisplayName("Debe extraer el rol del token")

    void shouldExtractRoleFromToken() {
        AuthCredentials credentials = new AuthCredentials();
        credentials.setDocument(123456789L);

        TokenResponse response = jwtAdapter.authenticate(credentials, "MESSENGER", 1L);
        String role = jwtAdapter.extractRole(response.getToken());

        assertEquals("MESSENGER", role);
    }

    @Test
    @DisplayName("Debe generar token de refresco")

    void shouldGenerateRefreshToken() {
        AuthCredentials credentials = new AuthCredentials();
        credentials.setDocument(123456789L);

        String refreshToken = jwtAdapter.generateRefreshToken(credentials);

        assertNotNull(refreshToken);
        assertTrue(jwtAdapter.validateRefreshToken(refreshToken));
    }
}
