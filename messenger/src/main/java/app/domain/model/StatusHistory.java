package app.domain.model;

import app.domain.model.enums.Status;
import java.time.LocalDateTime;

public class StatusHistory {
    private Long idStatusHistory;
    private ServiceDelivery serviceDelivery;
    private Status previousStatus;
    private Status newStatus;
    private LocalDateTime changeDate;
    private Employee changedBy; // El usuario (admin o mensajero) que hizo el cambio

    public Long getIdStatusHistory() {
        return idStatusHistory;
    }

    public void setIdStatusHistory(Long idStatusHistory) {
        this.idStatusHistory = idStatusHistory;
    }

    public ServiceDelivery getServiceDelivery() {
        return serviceDelivery;
    }

    public void setServiceDelivery(ServiceDelivery serviceDelivery) {
        this.serviceDelivery = serviceDelivery;
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

    public Employee getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(Employee changedBy) {
        this.changedBy = changedBy;
    }
}