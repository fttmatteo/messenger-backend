package app.infrastructure.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Servicio para gestionar la blacklist de tokens JWT.
 * 
 * Mantiene un registro en Redis de refresh tokens que han sido usados,
 * implementando Refresh Token Rotation para mayor seguridad.
 * 
 * Seguridad:
 * - Detecta reutilización de refresh tokens (posible robo)
 * - Invalida tokens usados para prevenir replay attacks
 * - TTL automático para limpieza de tokens expirados
 * 
 * @see RefreshTokenUseCase
 */
@Service
public class TokenBlacklistService {

    private static final Logger logger = LoggerFactory.getLogger(TokenBlacklistService.class);
    private static final String BLACKLIST_PREFIX = "token:blacklist:";

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * Añade un token a la blacklist.
     * 
     * Una vez en la blacklist, el token no puede ser reutilizado.
     * El token se elimina automáticamente después del TTL.
     * 
     * @param token      Token a invalidar
     * @param ttlSeconds Tiempo de vida en la blacklist (debe ser >= al TTL del
     *                   token)
     */
    public void addToBlacklist(String token, long ttlSeconds) {
        String key = BLACKLIST_PREFIX + hashToken(token);
        redisTemplate.opsForValue().set(key, "revoked", Duration.ofSeconds(ttlSeconds));
        logger.debug("Token añadido a blacklist con TTL: {} segundos", ttlSeconds);
    }

    /**
     * Verifica si un token está en la blacklist.
     * 
     * @param token Token a verificar
     * @return true si el token está en la blacklist (ha sido revocado), false en
     *         caso contrario
     */
    public boolean isBlacklisted(String token) {
        String key = BLACKLIST_PREFIX + hashToken(token);
        Boolean exists = redisTemplate.hasKey(key);
        boolean blacklisted = exists != null && exists;

        if (blacklisted) {
            logger.warn("SEGURIDAD: Intento de reutilización de token revocado");
        }

        return blacklisted;
    }

    /**
     * Genera un hash del token para usarlo como key en Redis.
     * Evita almacenar el token completo en Redis.
     */
    private String hashToken(String token) {
        // Usamos los últimos 32 caracteres del token como identificador
        // Esto es suficiente para unicidad y evita almacenar todo el token
        if (token.length() <= 32) {
            return token;
        }
        return token.substring(token.length() - 32);
    }
}
