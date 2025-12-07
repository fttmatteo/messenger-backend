// Archivo: app/domain/model/StatusHistory.java
package app.domain.model;

import app.domain.model.enums.Status;
import java.time.LocalDateTime;

public class StatusHistory {
    private Long idStatusHistory;
    private Status previousStatus;
    private Status newStatus;
    private LocalDateTime changeDate;
    private Employee changedBy; // Usuario auditor

    public Long getIdStatusHistory() { return idStatusHistory; }
    public void setIdStatusHistory(Long idStatusHistory) { this.idStatusHistory = idStatusHistory; }
    public Status getPreviousStatus() { return previousStatus; }
    public void setPreviousStatus(Status previousStatus) { this.previousStatus = previousStatus; }
    public Status getNewStatus() { return newStatus; }
    public void setNewStatus(Status newStatus) { this.newStatus = newStatus; }
    public LocalDateTime getChangeDate() { return changeDate; }
    public void setChangeDate(LocalDateTime changeDate) { this.changeDate = changeDate; }
    public Employee getChangedBy() { return changedBy; }
    public void setChangedBy(Employee changedBy) { this.changedBy = changedBy; }
}