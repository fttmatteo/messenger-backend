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
    /**
     * Estado inicial asignado automáticamente al detectar una placa.
     * Indica que el servicio ha sido creado pero no procesado.
     */
    ASSIGNED,

    /**
     * El servicio está en curso pero pendiente de entrega final.
     * Requisito de evidencia: Firma, Foto y Observación.
     */
    PENDING,

    /**
     * La entrega se ha completado exitosamente con el cliente.
     * Requisito de evidencia: Firma obligatoria.
     */
    DELIVERED,

    /**
     * El intento de entrega falló (ej: cliente no encontrado).
     * Requisito de evidencia: Firma (si aplica), Foto y Observación.
     */
    FAILED,

    /**
     * La placa o documento fue devuelto.
     * Requisito de evidencia: Firma, Foto y Observación.
     */
    RETURNED,

    /**
     * El servicio fue cancelado administrativamente.
     * Permiso: Solo administradores.
     */
    CANCELED,

    /**
     * El servicio presenta novedades y está bajo revisión.
     * Permiso: Solo administradores.
     */
    OBSERVED,

    /**
     * La observación o novedad ha sido solucionada.
     */
    RESOLVED
}