package app.domain.services;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Implementación sin operaciones (No-Op) del LoginRateLimitService.
 * Utilizada cuando Redis está deshabilitado (redis.enabled=false).
 * En este modo, el rate limiting no se aplica ya que no hay almacenamiento distribuido.
 */
@Service
@ConditionalOnProperty(name = "redis.enabled", havingValue = "false")
public class NoOpLoginRateLimitService extends LoginRateLimitService {

    /**
     * No bloquea a nadie - siempre retorna false.
     */
    @Override
    public boolean isBlocked(Long document) {
        return false;
    }

    /**
     * No registra intentos - siempre retorna el máximo de intentos permitidos.
     */
    @Override
    public int recordFailedAttempt(Long document) {
        return 4; // Retorna 4 intentos restantes (5 - 1)
    }

    /**
     * No hace nada al limpiar intentos.
     */
    @Override
    public void clearFailedAttempts(Long document) {
        // No-op
    }

    /**
     * Siempre retorna el máximo de intentos permitidos.
     */
    @Override
    public int getRemainingAttempts(Long document) {
        return 5; // MAX_FAILED_ATTEMPTS
    }
}
