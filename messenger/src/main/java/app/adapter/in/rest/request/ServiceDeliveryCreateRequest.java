package app.adapter.in.rest.request;

/**
 * DTO para crear un nuevo servicio de entrega.
 */
public class ServiceDeliveryCreateRequest {
    private String dealershipId;
    private String messengerId;
    private String manualPlateNumber;

    public ServiceDeliveryCreateRequest() {
    }

    public ServiceDeliveryCreateRequest(String dealershipId, String messengerId) {
        this.dealershipId = dealershipId;
        this.messengerId = messengerId;
    }

    public String getDealershipId() {
        return dealershipId;
    }

    public void setDealershipId(String dealershipId) {
        this.dealershipId = dealershipId;
    }

    public String getMessengerId() {
        return messengerId;
    }

    public void setMessengerId(String messengerId) {
        this.messengerId = messengerId;
    }

    public String getManualPlateNumber() {
        return manualPlateNumber;
    }

    public void setManualPlateNumber(String manualPlateNumber) {
        this.manualPlateNumber = manualPlateNumber;
    }
}
