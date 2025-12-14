package app.infrastructure.persistence.entities;

import app.domain.model.enums.Status;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad JPA que representa la tabla 'service_deliveries'.
 * 
 * Entidad central del sistema que vincula concesionarios, mensajeros, placas
 * y gestiona el ciclo de vida completo de una entrega con su historial.
 * 
 * Relaciones:
 * - Pertenece a una Plate (N:1)
 * - Pertenece a un Dealership destino (N:1)
 * - Asignado a un Employee mensajero (N:1)
 * - Tiene una Signature opcional (1:1)
 * - Tiene múltiples Photos de evidencia (1:N)
 * - Tiene múltiples StatusHistory para auditoría (1:N)
 */
@Entity
@Table(name = "service_deliveries")
public class ServiceDeliveryEntity {

    /** Identificador único del servicio de entrega (clave primaria). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_service_delivery")
    private Long idServiceDelivery;

    /**
     * Placa vehicular asociada a este servicio (EAGER para optimizar consultas).
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "plate_id", nullable = false)
    private PlateEntity plate;

    /** Concesionario destino de la entrega (EAGER para optimizar consultas). */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "dealership_id", nullable = false)
    private DealershipEntity dealership;

    /** Mensajero asignado al servicio (LAZY para evitar carga innecesaria). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "messenger_id", nullable = false)
    private EmployeeEntity messenger;

    /** Estado actual del servicio (ASSIGNED, PENDING, DELIVERED, etc). */
    @Enumerated(EnumType.STRING)
    @Column(name = "current_status", nullable = false)
    private Status currentStatus;

    /** Observaciones adicionales sobre el servicio. */
    private String observation;

    /** Firma digital de recepción (obligatoria para estado DELIVERED). */
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "signature_id", referencedColumnName = "id_signature")
    private SignatureEntity signature;

    /**
     * Fotos de evidencia asociadas al servicio (detección y evidencias de estado).
     */
    @OneToMany(mappedBy = "serviceDelivery", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PhotoEntity> photos = new ArrayList<>();

    /** Historial completo de cambios de estado para auditoría. */
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

    /** Callback JPA para establecer la fecha de creación automáticamente. */
    @PrePersist
    protected void onCreate() {
        createdAt = java.time.LocalDateTime.now();
    }

    public java.time.LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(java.time.LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}