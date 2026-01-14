package app.domain.model.enums;

/**
 * Estados posibles de un servicio de entrega.
 */
public enum Status {
    ASSIGNED,
    PENDING, 
    DELIVERED, 
    RETURNED, 
    CANCELED, 
    RESOLVED, 
    FAILED, 
    DELETED 
}