package app.infrastructure.persistence.entities;

import app.domain.model.enums.Status;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad JPA que representa la tabla 'service_deliveries'.
 */
@Entity
@Table(name = "service_deliveries")
public class ServiceDeliveryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_service_delivery")
    private Long idServiceDelivery;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "plate_id", nullable = false)
    private PlateEntity plate; // Placa del vehículo del servicio

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "dealership_id", nullable = false)
    private DealershipEntity dealership;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "messenger_id", nullable = false)
    private EmployeeEntity messenger;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_status", nullable = false)
    private Status currentStatus; // Estado actual del servicio

    private String observation;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "signature_id", referencedColumnName = "id_signature")
    private SignatureEntity signature; // Firma digital de la entrega

    @OneToMany(mappedBy = "serviceDelivery", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PhotoEntity> photos = new ArrayList<>();

    @OneToMany(mappedBy = "serviceDelivery", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StatusHistoryEntity> history = new ArrayList<>();

    public ServiceDeliveryEntity() {
    }

    public Long getIdServiceDelivery() {
        return idServiceDelivery;
    }

    public void setIdServiceDelivery(Long idServiceDelivery) {
        this.idServiceDelivery = idServiceDelivery;
    }

    public PlateEntity getPlate() {
        return plate;
    }

    public void setPlate(PlateEntity plate) {
        this.plate = plate;
    }

    public DealershipEntity getDealership() {
        return dealership;
    }

    public void setDealership(DealershipEntity dealership) {
        this.dealership = dealership;
    }

    public EmployeeEntity getMessenger() {
        return messenger;
    }

    public void setMessenger(EmployeeEntity messenger) {
        this.messenger = messenger;
    }

    public Status getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(Status currentStatus) {
        this.currentStatus = currentStatus;
    }

    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }

    public SignatureEntity getSignature() {
        return signature;
    }

    public void setSignature(SignatureEntity signature) {
        this.signature = signature;
    }

    public List<PhotoEntity> getPhotos() {
        return photos;
    }

    public void setPhotos(List<PhotoEntity> photos) {
        this.photos = photos;
    }

    public List<StatusHistoryEntity> getHistory() {
        return history;
    }

    public void setHistory(List<StatusHistoryEntity> history) {
        this.history = history;
    }

    /** Fecha y hora de creación del servicio (auto-generada). */
    @Column(name = "created_at", nullable = true, updatable = false)
    private java.time.LocalDateTime createdAt;

    /** Indica si el servicio está en la papelera (soft delete). */
    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    /** Fecha y hora en que el servicio fue movido a la papelera. */
    @Column(name = "deleted_at")
    private java.time.LocalDateTime deletedAt;

    /**
     * @deprecated Este campo ya no se utiliza. Anteriormente marcaba cuando un
     *             servicio
     *             entraba en ventana de 72h para edición (estados
     *             DELIVERED/RESOLVED).
     */
    @Column(name = "locked_at")
    private java.time.LocalDateTime lockedAt;

    /** Callback JPA para establecer la fecha de creación automáticamente. */
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = java.time.LocalDateTime.now();
        }
    }

    public java.time.LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(java.time.LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public java.time.LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(java.time.LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    public java.time.LocalDateTime getLockedAt() {
        return lockedAt;
    }

    public void setLockedAt(java.time.LocalDateTime lockedAt) {
        this.lockedAt = lockedAt;
    }
}