package app.domain.model.enums;

/**
 * Estado del tracking en tiempo real de un mensajero.
 */
public enum TrackingStatus {
    /**
     * Mensajero activamente en ruta, enviando ubicaciones.
     */
    ACTIVE,

    /**
     * Mensajero en pausa (almuerzo, descanso).
     */
    INACTIVE,

    /**
     * Mensajero sin conexión o app cerrada.
     */
    OFFLINE
}
