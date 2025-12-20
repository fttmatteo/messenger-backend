package app.adapter.in.rest.request;

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
}
