package app.adapter.in.rest.response;

import app.domain.model.enums.Status;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de respuesta con historial de cambio de estado.
 */
public class StatusHistoryResponse {
    private Long idStatusHistory;
    private Status previousStatus;
    private Status newStatus;
    private LocalDateTime changeDate;
    private EmployeeResponse changedBy;
    private List<PhotoResponse> photos;
    private Double deliveryLatitude;
    private Double deliveryLongitude;
    private String observation;

    public StatusHistoryResponse() {
    }

    public StatusHistoryResponse(Long idStatusHistory, Status previousStatus, Status newStatus,
            LocalDateTime changeDate, EmployeeResponse changedBy, Double deliveryLatitude, Double deliveryLongitude,
            String observation) {
        this.idStatusHistory = idStatusHistory;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.changeDate = changeDate;
        this.changedBy = changedBy;
        this.deliveryLatitude = deliveryLatitude;
        this.deliveryLongitude = deliveryLongitude;
        this.observation = observation;
    }

    public Long getIdStatusHistory() {
        return idStatusHistory;
    }

    public void setIdStatusHistory(Long idStatusHistory) {
        this.idStatusHistory = idStatusHistory;
    }

    public Status getPreviousStatus() {
        return previousStatus;
    }

    public void setPreviousStatus(Status previousStatus) {
        this.previousStatus = previousStatus;
    }

    public Status getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(Status newStatus) {
        this.newStatus = newStatus;
    }

    public LocalDateTime getChangeDate() {
        return changeDate;
    }

    public void setChangeDate(LocalDateTime changeDate) {
        this.changeDate = changeDate;
    }

    public EmployeeResponse getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(EmployeeResponse changedBy) {
        this.changedBy = changedBy;
    }

    public List<PhotoResponse> getPhotos() {
        return photos;
    }

    public void setPhotos(List<PhotoResponse> photos) {
        this.photos = photos;
    }

    public Double getDeliveryLatitude() {
        return deliveryLatitude;
    }

    public void setDeliveryLatitude(Double deliveryLatitude) {
        this.deliveryLatitude = deliveryLatitude;
    }

    public Double getDeliveryLongitude() {
        return deliveryLongitude;
    }

    public void setDeliveryLongitude(Double deliveryLongitude) {
        this.deliveryLongitude = deliveryLongitude;
    }

    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }
}
