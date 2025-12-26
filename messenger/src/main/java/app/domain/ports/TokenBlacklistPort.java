package app.domain.ports;

/**
 * Puerto de salida para gestión de tokens invalidados (Blacklist).
 */
public interface TokenBlacklistPort {
    /**
     * Agrega un token a la lista negra con un tiempo de vida (TTL).
     */
    void addToBlacklist(String token, long ttlSeconds);

    /**
     * Verifica si un token está en la lista negra.
     */
    boolean isBlacklisted(String token);
}
