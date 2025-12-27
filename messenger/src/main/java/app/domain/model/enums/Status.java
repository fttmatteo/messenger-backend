package app.domain.model.enums;

/**
 * Estados posibles de un servicio de entrega.
 */
public enum Status {
    ASSIGNED, // Asignado a un mensajero, en ruta inicial
    PENDING, // En sitio, pendiente de entregar
    DELIVERED, // Entregado exitosamente con firma
    RETURNED, // Devolución (ej. dirección errónea)
    CANCELED, // Cancelado por admin o cliente
    RESOLVED, // Resuelto manualmente tras incidencia
    FAILED, // Fallo general (obsoleto, usar RETURNED o CANCELED)
    DELETED // Marcado para eliminación
}