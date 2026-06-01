package app.adapter.out.persistence.entities;

import app.domain.model.enums.Status;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad JPA que representa la tabla 'status_history'.
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

    @OneToMany(mappedBy = "statusHistory", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PhotoEntity> photos = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "signature_id")
    private SignatureEntity signature;

    @Column(name = "observation", length = 2048)
    private String observation;

    @Column(name = "snapshot_origin_dealership_id")
    private Long snapshotOriginDealershipId;

    @Column(name = "snapshot_origin_dealership_name")
    private String snapshotOriginDealershipName;

    @Column(name = "snapshot_destination_dealership_id")
    private Long snapshotDestinationDealershipId;

    @Column(name = "snapshot_destination_dealership_name")
    private String snapshotDestinationDealershipName;

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

    public List<PhotoEntity> getPhotos() {
        return photos;
    }

    public void setPhotos(List<PhotoEntity> photos) {
        this.photos = photos;
    }

    public SignatureEntity getSignature() {
        return signature;
    }

    public void setSignature(SignatureEntity signature) {
        this.signature = signature;
    }

    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
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

    public Long getSnapshotOriginDealershipId() {
        return snapshotOriginDealershipId;
    }

    public void setSnapshotOriginDealershipId(Long snapshotOriginDealershipId) {
        this.snapshotOriginDealershipId = snapshotOriginDealershipId;
    }

    public String getSnapshotOriginDealershipName() {
        return snapshotOriginDealershipName;
    }

    public void setSnapshotOriginDealershipName(String snapshotOriginDealershipName) {
        this.snapshotOriginDealershipName = snapshotOriginDealershipName;
    }

    public Long getSnapshotDestinationDealershipId() {
        return snapshotDestinationDealershipId;
    }

    public void setSnapshotDestinationDealershipId(Long snapshotDestinationDealershipId) {
        this.snapshotDestinationDealershipId = snapshotDestinationDealershipId;
    }

    public String getSnapshotDestinationDealershipName() {
        return snapshotDestinationDealershipName;
    }

    public void setSnapshotDestinationDealershipName(String snapshotDestinationDealershipName) {
        this.snapshotDestinationDealershipName = snapshotDestinationDealershipName;
    }
}