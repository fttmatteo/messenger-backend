package app.domain.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import app.domain.model.enums.PlateType;
import app.domain.model.enums.Status;

public class Service {
    private Long idService;
    private Plate plate;
    private PlateType plateType;
    private Status status;
    private List<StatusHistory> statusHistory = new ArrayList<>();
    private LocalDateTime pendingDate, assignedDate, deliveredDate, failedDate,
            returnedDate, canceledDate, observedDate, resolvedDate;
    private Employee employee;
    private Dealership dealership;
    private String observation;
    private Signature signature;
    private Photo visitPhoto;

    public Long getIdService() {
        return idService;
    }

    public void setIdService(Long idService) {
        this.idService = idService;
    }

    public Plate getPlate() {
        return plate;
    }

    public void setPlate(Plate plate) {
        this.plate = plate;
    }

    public PlateType getPlateType() {
        return plateType;
    }

    public void setPlateType(PlateType plateType) {
        this.plateType = plateType;
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

    public LocalDateTime getPendingDate() {
        return pendingDate;
    }

    public void setPendingDate(LocalDateTime pendingDate) {
        this.pendingDate = pendingDate;
    }

    public LocalDateTime getAssignedDate() {
        return assignedDate;
    }

    public void setAssignedDate(LocalDateTime assignedDate) {
        this.assignedDate = assignedDate;
    }

    public LocalDateTime getDeliveredDate() {
        return deliveredDate;
    }

    public void setDeliveredDate(LocalDateTime deliveredDate) {
        this.deliveredDate = deliveredDate;
    }

    public LocalDateTime getFailedDate() {
        return failedDate;
    }

    public void setFailedDate(LocalDateTime failedDate) {
        this.failedDate = failedDate;
    }

    public LocalDateTime getReturnedDate() {
        return returnedDate;
    }

    public void setReturnedDate(LocalDateTime returnedDate) {
        this.returnedDate = returnedDate;
    }

    public LocalDateTime getCanceledDate() {
        return canceledDate;
    }

    public void setCanceledDate(LocalDateTime canceledDate) {
        this.canceledDate = canceledDate;
    }

    public LocalDateTime getObservedDate() {
        return observedDate;
    }

    public void setObservedDate(LocalDateTime observedDate) {
        this.observedDate = observedDate;
    }

    public LocalDateTime getResolvedDate() {
        return resolvedDate;
    }

    public void setResolvedDate(LocalDateTime resolvedDate) {
        this.resolvedDate = resolvedDate;
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

    public Photo getVisitPhoto() {
        return visitPhoto;
    }

    public void setVisitPhoto(Photo visitPhoto) {
        this.visitPhoto = visitPhoto;
    }
}