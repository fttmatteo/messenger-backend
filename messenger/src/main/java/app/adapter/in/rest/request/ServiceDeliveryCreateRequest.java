package app.adapter.in.rest.request;

public class ServiceDeliveryCreateRequest {
    private String dealershipId;
    private String messengerDocument;

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
}
