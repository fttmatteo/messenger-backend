package app.adapter.in.rest.response;

import app.domain.model.enums.PlateType;
import app.domain.model.enums.Status;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO de respuesta con datos completos de servicio de entrega.
 */
public class ServiceDeliveryResponse {
    private Long idServiceDelivery;
    private String uuid;
    private PlateResponse plate;
    private DealershipResponse dealership;
    private EmployeeResponse messenger;
    private Status currentStatus;
    private String observation;
    private SignatureResponse signature;
    private List<PhotoResponse> photos = new ArrayList<>();
    private List<StatusHistoryResponse> history = new ArrayList<>();
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime lockedAt;
    private java.time.LocalDateTime editDeadline;
    private boolean isLocked;
    private boolean deleted;
    private java.time.LocalDateTime deletedAt;

    public java.time.LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(java.time.LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public java.time.LocalDateTime getLockedAt() {
        return lockedAt;
    }

    public void setLockedAt(java.time.LocalDateTime lockedAt) {
        this.lockedAt = lockedAt;
    }

    public java.time.LocalDateTime getEditDeadline() {
        return editDeadline;
    }

    public void setEditDeadline(java.time.LocalDateTime editDeadline) {
        this.editDeadline = editDeadline;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public java.time.LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(java.time.LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    public ServiceDeliveryResponse() {
    }

    public Long getIdServiceDelivery() {
        return idServiceDelivery;
    }

    public void setIdServiceDelivery(Long idServiceDelivery) {
        this.idServiceDelivery = idServiceDelivery;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
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
