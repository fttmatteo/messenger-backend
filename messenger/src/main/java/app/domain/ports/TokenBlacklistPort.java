package app.domain.ports;

/**
 * Puerto de salida para gestión de tokens invalidados (Blacklist).
 */
public interface TokenBlacklistPort {
    void addToBlacklist(String token, long ttlSeconds);

    boolean isBlacklisted(String token);
}
