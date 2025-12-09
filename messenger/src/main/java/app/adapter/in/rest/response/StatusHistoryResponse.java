package app.adapter.in.rest.response;

import app.domain.model.enums.Status;
import java.time.LocalDateTime;

public class StatusHistoryResponse {
    private Long idStatusHistory;
    private Status previousStatus;
    private Status newStatus;
    private LocalDateTime changeDate;
    private EmployeeResponse changedBy;
    private java.util.List<PhotoResponse> photos;

    public StatusHistoryResponse() {
    }

    public StatusHistoryResponse(Long idStatusHistory, Status previousStatus, Status newStatus,
            LocalDateTime changeDate, EmployeeResponse changedBy) {
        this.idStatusHistory = idStatusHistory;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.changeDate = changeDate;
        this.changedBy = changedBy;
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

    public java.util.List<PhotoResponse> getPhotos() {
        return photos;
    }

    public void setPhotos(java.util.List<PhotoResponse> photos) {
        this.photos = photos;
    }
}
