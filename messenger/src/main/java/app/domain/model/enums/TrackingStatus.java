package app.domain.model.enums;

/**
 * Estado del tracking en tiempo real de un mensajero.
 * 
 * ACTIVE: Mensajero activamente en ruta, enviando ubicaciones.
 * INACTIVE: Mensajero en pausa (almuerzo, descanso).
 * OFFLINE: Mensajero sin conexión o app cerrada.
 */
public enum TrackingStatus {
    ACTIVE,
    INACTIVE,
    OFFLINE
}
