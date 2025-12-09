package app.infrastructure.persistence.entities;

import app.domain.model.enums.Status;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "service_deliveries")
public class ServiceDeliveryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_service_delivery")
    private Long idServiceDelivery;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "plate_id", nullable = false)
    private PlateEntity plate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "dealership_id", nullable = false)
    private DealershipEntity dealership;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "messenger_id", nullable = false)
    private EmployeeEntity messenger;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_status", nullable = false)
    private Status currentStatus;

    private String observation;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "signature_id", referencedColumnName = "id_signature")
    private SignatureEntity signature;

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
}