package app.adapter.in.rest.request;

/**
 * DTO (Data Transfer Object) para actualizar el estado de un servicio de
 * entrega.
 * 
 * Este objeto se utiliza para registrar cambios de estado en el ciclo de vida
 * de un servicio de entrega, manteniendo trazabilidad de quién realizó el
 * cambio
 * y las razones del mismo.
 * 
 * Campos incluidos:
 * - status: Nuevo estado del servicio (PENDING, IN_PROGRESS, DELIVERED,
 * CANCELED, etc.)
 * - observation: Observaciones o notas sobre el cambio (ej. razón de
 * cancelación)
 * - userDocument: Documento del usuario que registra el cambio (auditoría)
 * 
 * Dependiendo del estado, pueden ser requeridas evidencias adicionales como
 * firma digital, fotografías o coordenadas de ubicación.
 * 
 * @see app.adapter.in.rest.controllers.ServiceDeliveryController
 * @see app.domain.model.ServiceDelivery
 * @see app.domain.model.enums.ServiceStatus
 */
public class ServiceDeliveryUpdateStatusRequest {
    private String status;
    private String observation;
    private String userDocument;

    public ServiceDeliveryUpdateStatusRequest() {
    }

    public ServiceDeliveryUpdateStatusRequest(String status, String observation, String userDocument) {
        this.status = status;
        this.observation = observation;
        this.userDocument = userDocument;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }

    public String getUserDocument() {
        return userDocument;
    }

    public void setUserDocument(String userDocument) {
        this.userDocument = userDocument;
    }
}
