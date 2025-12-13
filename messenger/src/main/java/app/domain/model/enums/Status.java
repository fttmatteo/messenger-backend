package app.domain.model.enums;

/**
 * Enumeración de estados posibles para un servicio de entrega.
 * 
 * Los estados siguen un flujo de vida específico y determinan qué evidencias
 * son requeridas para cada transición.
 * 
 * Estados disponibles:
 * ASSIGNED: Estado automático al detectar placa inicial
 * PENDING: Pendiente de entrega (requiere: firma, foto, observación)
 * DELIVERED: Entrega exitosa (requiere: firma obligatoria)
 * FAILED: Entrega fallida (requiere: firma, foto, observación)
 * RETURNED: Placa devuelta (requiere: firma, foto, observación)
 * CANCELED: Cancelado administrativamente (solo admins)
 * OBSERVED: Bajo observación (solo admins)
 * RESOLVED: Resuelto tras observación
 */
public enum Status {
    ASSIGNED,
    PENDING,
    DELIVERED,
    FAILED,
    RETURNED,
    CANCELED,
    OBSERVED,
    RESOLVED
}