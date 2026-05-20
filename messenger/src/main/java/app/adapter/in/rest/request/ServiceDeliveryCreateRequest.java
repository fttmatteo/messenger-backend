package app.adapter.in.rest.request;

/**
 * DTO para crear un nuevo servicio de entrega.
 */
public class ServiceDeliveryCreateRequest {
    private String dealershipId;
    private String originDealershipId;
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

    public String getOriginDealershipId() {
        return originDealershipId;
    }

    public void setOriginDealershipId(String originDealershipId) {
        this.originDealershipId = originDealershipId;
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

    private Double latitude;
    private Double longitude;

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}
