package app.infrastructure.persistence.entities;

import app.domain.model.enums.Status;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad JPA para historial de cambios de estado archivados.
 */
@Entity
@Table(name = "deleted_status_history")
public class DeletedStatusHistoryEntity {

    @Id
    @Column(name = "id_status_history")
    private Long idStatusHistory;

    @Column(name = "service_delivery_id", nullable = false)
    private Long serviceDeliveryId;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status")
    private Status previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false)
    private Status newStatus;

    @Column(name = "change_date", nullable = false)
    private LocalDateTime changeDate;

    @Column(columnDefinition = "TEXT")
    private String observation;

    @Column(name = "changed_by_employee_id")
    private Long changedByEmployeeId;

    @Column(name = "changed_by_name")
    private String changedByName;

    @Column(name = "changed_by_document")
    private String changedByDocument;

    public DeletedStatusHistoryEntity() {
    }

    public Long getIdStatusHistory() {
        return idStatusHistory;
    }

    public void setIdStatusHistory(Long idStatusHistory) {
        this.idStatusHistory = idStatusHistory;
    }

    public Long getServiceDeliveryId() {
        return serviceDeliveryId;
    }

    public void setServiceDeliveryId(Long serviceDeliveryId) {
        this.serviceDeliveryId = serviceDeliveryId;
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

    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }

    public Long getChangedByEmployeeId() {
        return changedByEmployeeId;
    }

    public void setChangedByEmployeeId(Long changedByEmployeeId) {
        this.changedByEmployeeId = changedByEmployeeId;
    }

    public String getChangedByName() {
        return changedByName;
    }

    public void setChangedByName(String changedByName) {
        this.changedByName = changedByName;
    }

    public String getChangedByDocument() {
        return changedByDocument;
    }

    public void setChangedByDocument(String changedByDocument) {
        this.changedByDocument = changedByDocument;
    }
}
