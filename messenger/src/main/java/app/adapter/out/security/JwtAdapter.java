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
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;

/**
 * Adaptador de salida para generación y validación de tokens JWT (JSON Web
 * Tokens).
 * 
 * Este adaptador implementa AuthenticationPort y proporciona funcionalidades
 * completas
 * de autenticación basada en tokens JWT, incluyendo generación, validación y
 * extracción
 * de información de los tokens.
 * 
 * Responsabilidades:
 * - Generar tokens JWT firmados con información de usuario y rol
 * - Validar la autenticidad y vigencia de tokens
 * - Generar y validar Refresh Tokens para renovación de sesiones
 * - Extraer información (username, role) de tokens válidos
 * - Manejar errores de tokens expirados o inválidos
 * 
 * Configuración:
 * - jwt.secret: Clave secreta para firmar tokens (debe ser segura y privada)
 * - jwt.expiration: Tiempo de expiración del Access Token (default: 30 min)
 * - refreshExpirationTime: Tiempo de expiración del Refresh Token (calculado
 * como 24x el access token)
 * 
 * Algoritmo de firma: HS256 (HMAC con SHA-256)
 * 
 * Estructura del token:
 * - subject: Nombre de usuario
 * - claim "role": Rol del usuario (ADMIN, MESSENGER)
 * - issuedAt: Fecha de emisión
 * - expiration: Fecha de expiración
 * 
 * Seguridad:
 * - Los tokens están firmados digitalmente para prevenir manipulación
 * - La clave secreta se carga desde configuración externa
 * - Se valida la firma y expiración en cada validación
 * 
 * @see app.domain.ports.AuthenticationPort
 * @see app.domain.model.auth.TokenResponse
 * @see app.domain.model.auth.AuthCredentials
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
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationTime = expiration;
        this.refreshExpirationTime = expiration * 24; // 24 times the access token expiration (e.g., 1 day if access is
                                                      // 1h)
        logger.info("JwtAdapter inicializado con tiempo de expiración: {} ms y refresh: {} ms", expiration,
                refreshExpirationTime);
    }

    /**
     * Autentica un usuario y genera un token JWT.
     * 
     * Crea un token JWT firmado que contiene el nombre de usuario y rol,
     * el cual puede ser usado para autenticar peticiones subsecuentes.
     * 
     * @param credentials Credenciales del usuario (username, password)
     * @param role        Rol del usuario (ADMIN, MESSENGER)
     * @return TokenResponse con el token JWT generado y el rol
     */
    @Override
    public TokenResponse authenticate(AuthCredentials credentials, String role) {
        String token = this.generateToken(credentials.getUserName(), role);
        TokenResponse response = new TokenResponse();
        response.setToken(token);
        response.setRole(role);
        return response;
    }

    /**
     * Valida la autenticidad y vigencia de un token JWT.
     * 
     * Verifica:
     * - Firma digital del token
     * - Fecha de expiración
     * - Formato válido del token
     * 
     * @param token Token JWT a validar
     * @return true si el token es válido, false en caso contrario
     */
    @Override
    public boolean validateToken(String token) {
        try {
            this.getClaims(token);
            return true;
        } catch (SignatureException e) {
            logger.error("Firma JWT inválida");
            return false;
        } catch (MalformedJwtException e) {
            logger.error("Token JWT inválido: {}", e.getMessage());
            return false;
        } catch (ExpiredJwtException e) {
            logger.warn("Token expirado para usuario: {}", e.getClaims().getSubject());
            return false;
        } catch (UnsupportedJwtException e) {
            logger.error("Token JWT no soportado");
            return false;
        } catch (IllegalArgumentException e) {
            logger.error("Error inesperado al validar el token", e);
            return false;
        }
    }

    /**
     * Extrae el nombre de usuario del token JWT.
     * 
     * @param token Token JWT válido
     * @return Nombre de usuario contenido en el token
     */
    @Override
    public String extractUsername(String token) {
        Claims claims = this.getClaims(token);
        return claims.getSubject();
    }

    /**
     * Extrae el rol del usuario del token JWT.
     * 
     * @param token Token JWT válido
     * @return Rol del usuario (ADMIN, MESSENGER)
     */
    @Override
    public String extractRole(String token) {
        Claims claims = this.getClaims(token);
        return claims.get("role", String.class);
    }

    /**
     * Genera un nuevo token JWT firmado.
     * 
     * Crea un token con:
     * - Subject: nombre de usuario
     * - Claim "role": rol del usuario
     * - Fecha de emisión
     * - Fecha de expiración (configurada)
     * - Firma digital con HS256
     * 
     * @param userName Nombre de usuario
     * @param role     Rol del usuario
     * @return Token JWT firmado y codificado
     */
    private String generateToken(String userName, String role) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationTime);

        String token = Jwts.builder()
                .subject(userName) // setSubject -> subject
                .claim("role", role)
                .issuedAt(now) // setIssuedAt -> issuedAt
                .expiration(expiration) // setExpiration -> expiration
                .signWith(secretKey) // Removed SignatureAlgorithm arg
                .compact();

        return token;
    }

    @Override
    public String generateRefreshToken(AuthCredentials credentials) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + refreshExpirationTime);

        return Jwts.builder()
                .subject(credentials.getUserName())
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    @Override
    public boolean validateRefreshToken(String token) {
        return validateToken(token);
    }

    /**
     * Extrae los claims (datos) de un token JWT.
     * 
     * Parsea y valida el token, extrayendo toda la información contenida.
     * Lanza excepciones si el token es inválido o ha expirado.
     * 
     * @param token Token JWT a parsear
     * @return Claims contenidos en el token
     * @throws JwtException si el token es inválido
     */
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}