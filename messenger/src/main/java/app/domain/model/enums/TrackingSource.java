package app.domain.model.enums;

/**
 * Fuente de la ubicación del tracking.
 * 
 * GPS: Ubicación obtenida por GPS del dispositivo.
 * NETWORK: Ubicación obtenida por triangulación de red WiFi/celular.
 * MANUAL: Ubicación ingresada manualmente.
 */
public enum TrackingSource {
    /**
     * Ubicación obtenida directamente del sensor GPS del dispositivo.
     * Alta precisión.
     */
    GPS,

    /**
     * Ubicación estimada mediante triangulación de redes (WiFi, Celular).
     * Precisión media/baja.
     */
    NETWORK,

    /**
     * Ubicación ingresada o corregida manualmente por el usuario.
     */
    MANUAL
}
