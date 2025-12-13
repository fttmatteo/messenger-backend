package app.domain.model.enums;

/**
 * Fuente de la ubicación del tracking.
 * 
 * GPS: Ubicación obtenida por GPS del dispositivo.
 * NETWORK: Ubicación obtenida por triangulación de red WiFi/celular.
 * MANUAL: Ubicación ingresada manualmente.
 */
public enum TrackingSource {
    GPS,
    NETWORK,
    MANUAL
}
