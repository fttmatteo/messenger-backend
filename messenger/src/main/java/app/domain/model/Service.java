package app.domain.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import app.domain.model.enums.PlateType;
import app.domain.model.enums.Status;

public class Service {
    private Long id_service;
    private Plate plate;
    private PlateType plate_type;
    private Status status;
    private List<StatusHistory> statusHistory = new ArrayList<>();
    private LocalDateTime pending_date, assigned_date, delivered_date, failed_date,
            returned_date, canceled_date, observed_date, resolved_date;
    private Employee employee;
    private Dealership dealership;
    private String observation;
    private Signature signature;
    private Photo visit_photo;

    public Long getId_service() {
        return id_service;
    }

    public void setId_service(Long id_service) {
        this.id_service = id_service;
    }

    public Plate getPlate() {
        return plate;
    }

    public void setPlate(Plate plate) {
        this.plate = plate;
    }

    public PlateType getPlate_type() {
        return plate_type;
    }

    public void setPlate_type(PlateType plate_type) {
        this.plate_type = plate_type;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public List<StatusHistory> getStatusHistory() {
        return statusHistory;
    }

    public void setStatusHistory(List<StatusHistory> statusHistory) {
        this.statusHistory = statusHistory == null ? new ArrayList<>() : statusHistory;
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

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Dealership getDealership() {
        return dealership;
    }

    public void setDealership(Dealership dealership) {
        this.dealership = dealership;
    }

    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }

    public Signature getSignature() {
        return signature;
    }

    public void setSignature(Signature signature) {
        this.signature = signature;
    }

    public Photo getVisit_photo() {
        return visit_photo;
    }

    public void setVisit_photo(Photo visit_photo) {
        this.visit_photo = visit_photo;
    }
}