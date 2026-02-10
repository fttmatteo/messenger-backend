package app.domain.ports;

import java.util.Optional;

/**
 * Port para manejar el cacheo distribuido de URLs firmadas de almacenamiento.
 */
public interface StorageCachePort {

    /**
     * Obtiene una URL cacheada.
     */
    Optional<String> getUrl(String objectName);

    /**
     * Almacena una URL en el caché.
     * 
     * @param objectName        Nombre del objeto (clave)
     * @param url               URL firmada
     * @param expirationSeconds Tiempo de vida en el caché
     */
    void cacheUrl(String objectName, String url, long expirationSeconds);

    /**
     * Elimina una URL del caché.
     */
    void evictUrl(String objectName);
}
