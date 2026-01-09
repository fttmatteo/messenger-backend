package app.adapter.out.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import app.domain.model.auth.AuthCredentials;
import app.domain.model.auth.TokenResponse;
import app.domain.ports.AuthenticationPort;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;

/**
 * Adapter de JWT para generación y validación de tokens.
 */
@Component
public class JwtAdapter implements AuthenticationPort {

    private static final Logger logger = LoggerFactory.getLogger(JwtAdapter.class);

    private final SecretKey secretKey;
    private final long expirationTime;
    private final long refreshExpirationTime;

    public JwtAdapter(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration:1800000}") long expiration) {

        if (secret == null || secret.length() < 32) {
            throw new IllegalArgumentException(
                    "SEGURIDAD: JWT_SECRET debe tener al menos 32 caracteres (256 bits) para ser seguro. " +
                            "El valor actual tiene " + (secret == null ? 0 : secret.length()) + " caracteres. " +
                            "Genere uno con: openssl rand -base64 64");
        }

        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationTime = expiration;
        this.refreshExpirationTime = expiration * 24;
    }

    /**
     * Genera un token de acceso para un usuario autenticado.
     */
    @Override
    public TokenResponse authenticate(AuthCredentials credentials, String role, Long userId) {
        String token = this.generateToken(credentials.getDocument().toString(), role, userId);
        TokenResponse response = new TokenResponse();
        response.setToken(token);
        response.setRole(role);
        return response;
    }

    /**
     * Valida si un token JWT es auténtico y no ha expirado.
     */
    @Override
    public boolean validateToken(String token) {
        try {
            this.getClaims(token);
            return true;
        } catch (SignatureException e) {
            logger.warn("JWT inválido por firma: {}", e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            logger.warn("JWT malformado: {}", e.getMessage());
            return false;
        } catch (ExpiredJwtException e) {
            logger.warn("JWT expirado: {}", e.getMessage());
            return false;
        } catch (UnsupportedJwtException e) {
            logger.warn("JWT no soportado: {}", e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            logger.warn("JWT argumento inválido. Hash: {} - Error: {}", token.hashCode(), e.getMessage());
            return false;
        }
    }

    /**
     * Extrae el nombre de usuario (subject) del token.
     */
    @Override
    public String extractUsername(String token) {
        Claims claims = this.getClaims(token);
        return claims.getSubject();
    }

    /**
     * Extrae el rol del usuario del token.
     */
    @Override
    public String extractRole(String token) {
        Claims claims = this.getClaims(token);
        return claims.get("role", String.class);
    }

    @Override
    public String generateShortLivedToken(String username, String role, Long userId, long durationMs) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + durationMs);

        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .claim("id", userId)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    private String generateToken(String document, String role, Long userId) {
        return generateShortLivedToken(document, role, userId, expirationTime);
    }

    /**
     * Genera un refresh token de larga duración.
     */
    @Override
    public String generateRefreshToken(AuthCredentials credentials) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + refreshExpirationTime);

        return Jwts.builder()
                .subject(credentials.getDocument().toString())
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    /**
     * Valida la integridad y vigencia de un refresh token.
     */
    @Override
    public boolean validateRefreshToken(String token) {
        return validateToken(token);
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}