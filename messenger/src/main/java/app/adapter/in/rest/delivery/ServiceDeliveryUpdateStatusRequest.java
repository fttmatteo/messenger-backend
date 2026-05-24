package app.adapter.in.rest.delivery;

/**
 * DTO para actualizar estado de un servicio de entrega.
 */
public class ServiceDeliveryUpdateStatusRequest {
    private String status;
    private String observation;
    private String userId;

    public ServiceDeliveryUpdateStatusRequest() {
    }

    public ServiceDeliveryUpdateStatusRequest(String status, String observation, String userId) {
        this.status = status;
        this.observation = observation;
        this.userId = userId;
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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
