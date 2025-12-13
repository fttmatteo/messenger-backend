package app.infrastructure.persistence.entities;

import app.domain.model.enums.Status;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad JPA que representa la tabla 'status_history'.
 * Registra la auditoría completa de cambios de estado en los servicios.
 */
@Entity
@Table(name = "status_history")
public class StatusHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_status_history")
    private Long idStatusHistory;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status")
    private Status previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false)
    private Status newStatus;

    @Column(name = "change_date", nullable = false)
    private LocalDateTime changeDate;

    @ManyToOne
    @JoinColumn(name = "changed_by_employee_id")
    private EmployeeEntity changedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_delivery_id")
    private ServiceDeliveryEntity serviceDelivery;

    @Column(name = "delivery_latitude")
    private Double deliveryLatitude;

    @Column(name = "delivery_longitude")
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

    public EmployeeEntity getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(EmployeeEntity changedBy) {
        this.changedBy = changedBy;
    }

    @OneToMany(mappedBy = "statusHistory", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<PhotoEntity> photos = new java.util.ArrayList<>();

    public java.util.List<PhotoEntity> getPhotos() {
        return photos;
    }

    public void setPhotos(java.util.List<PhotoEntity> photos) {
        this.photos = photos;
    }

    public ServiceDeliveryEntity getServiceDelivery() {
        return serviceDelivery;
    }

    public void setServiceDelivery(ServiceDeliveryEntity serviceDelivery) {
        this.serviceDelivery = serviceDelivery;
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
}