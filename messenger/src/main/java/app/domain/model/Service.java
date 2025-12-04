package app.domain.model;

import java.sql.Date;
import app.domain.model.enums.*;

public class Service {
    private Long id_service;
    private String plate;
    private TypePlate type_plate;
    private Status status;
    private Date pending_date;
    private Date assigned_date;
    private Date delivered_date;
    private Date returned_date;
    private Date observed_date;
    private Date resolved_date;
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

    public TypePlate getType_plate() {
        return type_plate;
    }

    public void setType_plate(TypePlate type_plate) {
        this.type_plate = type_plate;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Date getPending_date() {
        return pending_date;
    }

    public void setPending_date(Date pending_date) {
        this.pending_date = pending_date;
    }

    public Date getAssigned_date() {
        return assigned_date;
    }

    public void setAssigned_date(Date assigned_date) {
        this.assigned_date = assigned_date;
    }

    public Date getDelivered_date() {
        return delivered_date;
    }

    public void setDelivered_date(Date delivered_date) {
        this.delivered_date = delivered_date;
    }

    public Date getReturned_date() {
        return returned_date;
    }

    public void setReturned_date(Date returned_date) {
        this.returned_date = returned_date;
    }

    public Date getObserved_date() {
        return observed_date;
    }

    public void setObserved_date(Date observed_date) {
        this.observed_date = observed_date;
    }

    public Date getResolved_date() {
        return resolved_date;
    }

    public void setResolved_date(Date resolved_date) {
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