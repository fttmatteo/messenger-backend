package app.adapter.in.rest.response;

import app.domain.model.enums.PlateType;
import app.domain.model.enums.Status;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO de respuesta detallada de un servicio de entrega.
 * 
 * Agrupa toda la información del servicio incluyendo placa, concesionario,
 * mensajero, estado actual, evidencias y el historial completo de cambios.
 */
public class ServiceDeliveryResponse {
    private Long idServiceDelivery;
    private PlateResponse plate;
    private DealershipResponse dealership;
    private EmployeeResponse messenger;
    private Status currentStatus;
    private String observation;
    private SignatureResponse signature;
    private List<PhotoResponse> photos = new ArrayList<>();
    private List<StatusHistoryResponse> history = new ArrayList<>();
    private java.time.LocalDateTime createdAt;

    public java.time.LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(java.time.LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public ServiceDeliveryResponse() {
    }

    public Long getIdServiceDelivery() {
        return idServiceDelivery;
    }

    public void setIdServiceDelivery(Long idServiceDelivery) {
        this.idServiceDelivery = idServiceDelivery;
    }

    public PlateResponse getPlate() {
        return plate;
    }

    public void setPlate(PlateResponse plate) {
        this.plate = plate;
    }

    public DealershipResponse getDealership() {
        return dealership;
    }

    public void setDealership(DealershipResponse dealership) {
        this.dealership = dealership;
    }

    public EmployeeResponse getMessenger() {
        return messenger;
    }

    public void setMessenger(EmployeeResponse messenger) {
        this.messenger = messenger;
    }

    public Status getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(Status currentStatus) {
        this.currentStatus = currentStatus;
    }

    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }

    public SignatureResponse getSignature() {
        return signature;
    }

    public void setSignature(SignatureResponse signature) {
        this.signature = signature;
    }

    public List<PhotoResponse> getPhotos() {
        return photos;
    }

    public void setPhotos(List<PhotoResponse> photos) {
        this.photos = photos;
    }

    public List<StatusHistoryResponse> getHistory() {
        return history;
    }

    public void setHistory(List<StatusHistoryResponse> history) {
        this.history = history;
    }

    // Nested PlateResponse to avoid circular dependencies
    public static class PlateResponse {
        private Long idPlate;
        private String plateNumber;
        private PlateType plateType;

        public PlateResponse() {
        }

        public PlateResponse(Long idPlate, String plateNumber, PlateType plateType) {
            this.idPlate = idPlate;
            this.plateNumber = plateNumber;
            this.plateType = plateType;
        }

        public Long getIdPlate() {
            return idPlate;
        }

        public void setIdPlate(Long idPlate) {
            this.idPlate = idPlate;
        }

        public String getPlateNumber() {
            return plateNumber;
        }

        public void setPlateNumber(String plateNumber) {
            this.plateNumber = plateNumber;
        }

        public PlateType getPlateType() {
            return plateType;
        }

        public void setPlateType(PlateType plateType) {
            this.plateType = plateType;
        }
    }
}
