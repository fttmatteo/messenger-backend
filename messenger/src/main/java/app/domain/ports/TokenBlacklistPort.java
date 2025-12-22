package app.domain.ports;

public interface TokenBlacklistPort {
    void addToBlacklist(String token, long ttlSeconds);

    boolean isBlacklisted(String token);
}
