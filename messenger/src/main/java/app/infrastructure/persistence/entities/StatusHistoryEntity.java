package app.infrastructure.persistence.entities;

import app.domain.model.enums.Status;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad JPA que representa la tabla 'status_history'.
 * 
 * Registra la auditoría completa de cambios de estado en los servicios de
 * entrega,
 * incluyendo quién realizó el cambio, cuándo, y la ubicación donde se realizó.
 * 
 * Relaciones:
 * - Pertenece a un ServiceDelivery (N:1)
 * - Registra el Employee que realizó el cambio (N:1)
 * - Puede tener múltiples Photos de evidencia (1:N)
 */
@Entity
@Table(name = "status_history")
public class StatusHistoryEntity {

    /** Identificador único del registro de historial (clave primaria). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_status_history")
    private Long idStatusHistory;

    /**
     * Estado anterior del servicio antes del cambio (null para el primer registro).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status")
    private Status previousStatus;

    /** Nuevo estado al que cambió el servicio. */
    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false)
    private Status newStatus;

    /** Fecha y hora exacta en que se realizó el cambio de estado. */
    @Column(name = "change_date", nullable = false)
    private LocalDateTime changeDate;

    /** Empleado que realizó el cambio de estado. */
    @ManyToOne
    @JoinColumn(name = "changed_by_employee_id")
    private EmployeeEntity changedBy;

    /** Servicio de entrega al que pertenece este registro de historial. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_delivery_id")
    private ServiceDeliveryEntity serviceDelivery;

    /** Latitud donde se realizó el cambio de estado (para tracking geográfico). */
    @Column(name = "delivery_latitude")
    private Double deliveryLatitude;

    /** Longitud donde se realizó el cambio de estado (para tracking geográfico). */
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

    /** Fotos de evidencia asociadas a este cambio de estado específico. */
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