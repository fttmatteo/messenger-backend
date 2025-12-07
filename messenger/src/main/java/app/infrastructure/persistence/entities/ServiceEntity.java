package app.infrastructure.persistence.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "services")
public class ServiceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idService;

    @OneToOne
    @JoinColumn(name = "id_plate", nullable = false)
    private PlateEntity plate;

    @Column(nullable = false)
    private String status;

    @ManyToOne
    @JoinColumn(name = "id_employee")
    private EmployeeEntity employee;

    @ManyToOne
    @JoinColumn(name = "id_dealership")
    private DealershipEntity dealership;

    @OneToMany(mappedBy = "service", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StatusHistoryEntity> statusHistory = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "id_signature")
    private SignatureEntity signature;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "id_visit_photo")
    private PhotoEntity visitPhoto;

    private String observation;

    private LocalDateTime assignedDate;
    private LocalDateTime deliveredDate;
    private LocalDateTime pendingDate;
    private LocalDateTime failedDate;
    private LocalDateTime returnedDate;
    private LocalDateTime canceledDate;
    private LocalDateTime observedDate;
    private LocalDateTime resolvedDate;

    @PrePersist
    protected void onCreate() {
        if (this.assignedDate == null) {
            this.assignedDate = LocalDateTime.now();
        }
    }

    public Long getIdService() {
        return idService;
    }

    public void setIdService(Long idService) {
        this.idService = idService;
    }

    public PlateEntity getPlate() {
        return plate;
    }

    public void setPlate(PlateEntity plate) {
        this.plate = plate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public EmployeeEntity getEmployee() {
        return employee;
    }

    public void setEmployee(EmployeeEntity employee) {
        this.employee = employee;
    }

    public DealershipEntity getDealership() {
        return dealership;
    }

    public void setDealership(DealershipEntity dealership) {
        this.dealership = dealership;
    }

    public List<StatusHistoryEntity> getStatusHistory() {
        return statusHistory;
    }

    public void setStatusHistory(List<StatusHistoryEntity> statusHistory) {
        this.statusHistory = statusHistory;
    }

    public SignatureEntity getSignature() {
        return signature;
    }

    public void setSignature(SignatureEntity signature) {
        this.signature = signature;
    }

    public PhotoEntity getVisitPhoto() {
        return visitPhoto;
    }

    public void setVisitPhoto(PhotoEntity visitPhoto) {
        this.visitPhoto = visitPhoto;
    }

    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
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

    public LocalDateTime getPendingDate() {
        return pendingDate;
    }

    public void setPendingDate(LocalDateTime pendingDate) {
        this.pendingDate = pendingDate;
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
}