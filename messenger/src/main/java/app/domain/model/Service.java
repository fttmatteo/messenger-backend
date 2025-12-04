package app.domain.model;

import java.time.LocalDateTime;
import app.domain.model.enums.PlateType;
import app.domain.model.enums.Status;

public class Service {
    private Long id_service;
    private String plate;
    private PlateType type_plate;
    private Status status;
    private LocalDateTime pending_date, assigned_date, delivered_date, failed_date, 
    returned_date, canceled_date, observed_date, resolved_date;
    private Employee messenger;
    private String observation;

    public Long getId_service() {
        return id_service;
    }

    public void setId_service(Long id_service) {
        this.id_service = id_service;
    }

    public String getPlate() {
        return plate;
    }

    public void setPlate(String plate) {
        this.plate = plate;
    }

    public PlateType getType_plate() {
        return type_plate;
    }

    public void setType_plate(PlateType type_plate) {
        this.type_plate = type_plate;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public LocalDateTime getPending_date() {
        return pending_date;
    }

    public void setPending_date(LocalDateTime pending_date) {
        this.pending_date = pending_date;
    }

    public LocalDateTime getAssigned_date() {
        return assigned_date;
    }

    public void setAssigned_date(LocalDateTime assigned_date) {
        this.assigned_date = assigned_date;
    }

    public LocalDateTime getDelivered_date() {
        return delivered_date;
    }

    public void setDelivered_date(LocalDateTime delivered_date) {
        this.delivered_date = delivered_date;
    }

    public LocalDateTime getFailed_date() {
        return failed_date;
    }

    public void setFailed_date(LocalDateTime failed_date) {
        this.failed_date = failed_date;
    }

    public LocalDateTime getReturned_date() {
        return returned_date;
    }

    public void setReturned_date(LocalDateTime returned_date) {
        this.returned_date = returned_date;
    }

    public LocalDateTime getCanceled_date() {
        return canceled_date;
    }

    public void setCanceled_date(LocalDateTime canceled_date) {
        this.canceled_date = canceled_date;
    }

    public LocalDateTime getObserved_date() {
        return observed_date;
    }

    public void setObserved_date(LocalDateTime observed_date) {
        this.observed_date = observed_date;
    }

    public LocalDateTime getResolved_date() {
        return resolved_date;
    }

    public void setResolved_date(LocalDateTime resolved_date) {
        this.resolved_date = resolved_date;
    }

    public Employee getMessenger() {
        return messenger;
    }

    public void setMessenger(Employee messenger) {
        this.messenger = messenger;
    }

    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }
}