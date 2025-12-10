package app.domain.model;

import app.domain.model.enums.Status;
import java.time.LocalDateTime;

public class StatusHistory {
    private Long idStatusHistory;
    private Status previousStatus;
    private Status newStatus;
    private LocalDateTime changeDate;
    private Employee changedBy;
    private Double deliveryLatitude;
    private Double deliveryLongitude;

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

    private java.util.List<Photo> photos;

    public Employee getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(Employee changedBy) {
        this.changedBy = changedBy;
    }

    public java.util.List<Photo> getPhotos() {
        return photos;
    }

    public void setPhotos(java.util.List<Photo> photos) {
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

    /**
     * Obtiene la ubicación de entrega como un objeto Location.
     * 
     * @return Location o null si no hay ubicación registrada
     */
    public Location getDeliveryLocation() {
        if (deliveryLatitude == null || deliveryLongitude == null) {
            return null;
        }
        return new Location(deliveryLatitude, deliveryLongitude);
    }

    /**
     * Establece la ubicación de entrega desde un objeto Location.
     */
    public void setDeliveryLocation(Location location) {
        if (location != null) {
            this.deliveryLatitude = location.getLatitude();
            this.deliveryLongitude = location.getLongitude();
        }
    }
}