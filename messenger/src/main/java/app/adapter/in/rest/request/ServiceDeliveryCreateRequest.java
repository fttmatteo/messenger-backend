package app.adapter.in.rest.request;

public class ServiceDeliveryCreateRequest {
    private String dealershipId;
    private String messengerDocument;
    private String manualPlateNumber; // NUEVO: Fallback si OCR falla

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
