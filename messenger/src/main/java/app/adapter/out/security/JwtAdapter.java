package app.adapter.out.security;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
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
import io.jsonwebtoken.SignatureAlgorithm;
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
 * - Extraer información (username, role) de tokens válidos
 * - Manejar errores de tokens expirados o inválidos
 * 
 * Configuración:
 * - jwt.secret: Clave secreta para firmar tokens (debe ser segura y privada)
 * - jwt.expiration: Tiempo de expiración en milisegundos (default: 1800000ms =
 * 30 min)
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

    private final Key secretKey;
    private final long expirationTime;

    public JwtAdapter(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration:1800000}") long expiration) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationTime = expiration;
        logger.info("JwtAdapter initialized with expiration time: {} ms", expiration);
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
        } catch (ExpiredJwtException e) {
            logger.warn("Token expired for user: {}", e.getClaims().getSubject());
            return false;
        } catch (SignatureException e) {
            logger.error("Invalid JWT signature");
            return false;
        } catch (JwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            logger.error("Unexpected error validating token", e);
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
                .setSubject(userName)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();

        return token;
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
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims;
    }
}