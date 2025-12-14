package app.domain.model.enums;

/**
 * Estado del tracking en tiempo real de un mensajero.
 * 
 * ACTIVE: Mensajero activamente en ruta, enviando ubicaciones.
 * INACTIVE: Mensajero en pausa (almuerzo, descanso).
 * OFFLINE: Mensajero sin conexión o app cerrada.
 */
public enum TrackingStatus {
    /**
     * El mensajero está en ruta y transmitiendo activamente su ubicación.
     */
    ACTIVE,

    /**
     * El mensajero está pausado temporalmente (ej: hora de almuerzo, descanso),
     * pero sigue conectado.
     */
    INACTIVE,

    /**
     * El mensajero se ha desconectado o cerrado la sesión.
     * No se reciben actualizaciones de ubicación.
     */
    OFFLINE
}
