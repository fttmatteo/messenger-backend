package app.adapter.in.rest.request;

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
