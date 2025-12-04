package app.domain.model;

import java.time.LocalDateTime;
import app.domain.model.enums.Status;

public class StatusHistory {
    private Long id_status_history;
    private Status previous_status;
    private Status status;
    private LocalDateTime change_date;
    private Service service;

    public Long getId_status_history() {
        return id_status_history;
    }

    public void setId_status_history(Long id_status_history) {
        this.id_status_history = id_status_history;
    }

    public Status getPrevious_status() {
        return previous_status;
    }

    public void setPrevious_status(Status previous_status) {
        this.previous_status = previous_status;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public LocalDateTime getChange_date() {
        return change_date;
    }

    public void setChange_date(LocalDateTime change_date) {
        this.change_date = change_date;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

}
