package app.adapter.in.rest.request;

/**
 * DTO (Data Transfer Object) para la creación de nuevos servicios de entrega.
 * 
 * Este objeto encapsula la información mínima necesaria para iniciar un
 * servicio
 * de entrega, estableciendo la relación entre un concesionario y un mensajero.
 * 
 * Campos incluidos:
 * - dealershipId: ID del concesionario que solicita el servicio
 * - messengerDocument: Documento del mensajero asignado (opcional en creación
 * inicial)
 * - manualPlateNumber: Número de placa del vehículo (si se ingresa manualmente)
 * 
 * El servicio se crea inicialmente en estado PENDING y puede incluir detección
 * automática
 * de placa mediante OCR si se proporciona una imagen.
 * 
 * @see app.adapter.in.rest.controllers.ServiceDeliveryController
 * @see app.domain.model.ServiceDelivery
 * @see app.domain.model.enums.ServiceStatus
 */
public class ServiceDeliveryCreateRequest {
    private String dealershipId;
    private String messengerDocument;
    private String manualPlateNumber;

    public ServiceDeliveryCreateRequest() {
    }

    public ServiceDeliveryCreateRequest(String dealershipId, String messengerDocument) {
        this.dealershipId = dealershipId;
        this.messengerDocument = messengerDocument;
    }

    public String getDealershipId() {
        return dealershipId;
    }

    public void setDealershipId(String dealershipId) {
        this.dealershipId = dealershipId;
    }

    public String getMessengerDocument() {
        return messengerDocument;
    }

    public void setMessengerDocument(String messengerDocument) {
        this.messengerDocument = messengerDocument;
    }

    public String getManualPlateNumber() {
        return manualPlateNumber;
    }

    public void setManualPlateNumber(String manualPlateNumber) {
        this.manualPlateNumber = manualPlateNumber;
    }
}
