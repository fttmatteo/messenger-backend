package app.domain.ports;

import java.util.Optional;

/**
 * Port para manejar el cacheo distribuido de URLs firmadas de almacenamiento.
 */
public interface StorageCachePort {
    Optional<String> getUrl(String objectName);

    void cacheUrl(String objectName, String url, long expirationSeconds);

    void evictUrl(String objectName);
}
