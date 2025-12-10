package app.domain.model.enums;

/**
 * Fuente de la ubicación del tracking.
 */
public enum TrackingSource {
    /**
     * Ubicación obtenida por GPS del dispositivo.
     * Mayor precisión (1-10 metros).
     */
    GPS,

    /**
     * Ubicación obtenida por triangulación de red WiFi/celular.
     * Menor precisión (10-100 metros).
     */
    NETWORK,

    /**
     * Ubicación ingresada manualmente.
     * Usada para correcciones o cuando no hay GPS disponible.
     */
    MANUAL
}
