package app.adapter.out.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
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

    private final SecretKey secretKey;
    private final long expirationTime;
    private final long refreshExpirationTime;

    public JwtAdapter(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration:1800000}") long expiration) {

        if (secret == null || secret.length() < 32) {
            throw new IllegalArgumentException(
                    "SEGURIDAD: JWT_SECRET debe tener al menos 32 caracteres (256 bits) para ser seguro. " +
                            "Genere uno con: openssl rand -base64 64");
        }

        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationTime = expiration;
        this.refreshExpirationTime = expiration * 24; // 24 times the access token expiration
    }

    @Override
    public TokenResponse authenticate(AuthCredentials credentials, String role, Long userId) {
        String token = this.generateToken(credentials.getDocument().toString(), role, userId);
        TokenResponse response = new TokenResponse();
        response.setToken(token);
        response.setRole(role);
        return response;
    }

    @Override
    public boolean validateToken(String token) {
        try {
            this.getClaims(token);
            return true;
        } catch (SignatureException e) {
            return false;
        } catch (MalformedJwtException e) {
            return false;
        } catch (ExpiredJwtException e) {
            return false;
        } catch (UnsupportedJwtException e) {
            return false;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public String extractUsername(String token) {
        Claims claims = this.getClaims(token);
        return claims.getSubject();
    }

    @Override
    public String extractRole(String token) {
        Claims claims = this.getClaims(token);
        return claims.get("role", String.class);
    }

    private String generateToken(String document, String role, Long userId) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationTime);

        String token = Jwts.builder()
                .subject(document)
                .claim("role", role)
                .claim("id", userId)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();

        return token;
    }

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