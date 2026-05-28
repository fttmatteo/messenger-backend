package app.domain.model;

import app.domain.model.enums.Status;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa un servicio de entrega de chasis vehicular.
 * Contiene la información del vehículo, destino, mensajero asignado e historial
 * de estados.
 */
public class ServiceDelivery {
    private Long idServiceDelivery;
    private String uuid;
    private Plate plate;
    private Dealership dealership;
    private Dealership originDealership;
    private Employee messenger;
    private Status currentStatus;
    private String observation;
    private Signature signature;
    private List<Photo> photos = new ArrayList<>();
    private List<StatusHistory> history = new ArrayList<>();

    public void addPhoto(Photo photo) {
        this.photos.add(photo);
    }

    public void addHistory(StatusHistory statusHistory) {
        this.history.add(statusHistory);
    }

    public Long getIdServiceDelivery() {
        return idServiceDelivery;
    }

    public void setIdServiceDelivery(Long idServiceDelivery) {
        this.idServiceDelivery = idServiceDelivery;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Plate getPlate() {
        return plate;
    }

    public void setPlate(Plate plate) {
        this.plate = plate;
    }

    public Dealership getDealership() {
        return dealership;
    }

    public void setDealership(Dealership dealership) {
        this.dealership = dealership;
    }

    public Dealership getOriginDealership() {
        return originDealership;
    }

    public void setOriginDealership(Dealership originDealership) {
        this.originDealership = originDealership;
    }

    public Employee getMessenger() {
        return messenger;
    }

    public void setMessenger(Employee messenger) {
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

    public Signature getSignature() {
        return signature;
    }

    public void setSignature(Signature signature) {
        this.signature = signature;
    }

    public List<Photo> getPhotos() {
        return photos;
    }

    public void setPhotos(List<Photo> photos) {
        this.photos = photos;
    }

    public List<StatusHistory> getHistory() {
        return history;
    }

    public void setHistory(List<StatusHistory> history) {
        this.history = history;
    }

    private java.time.LocalDateTime createdAt;
    private boolean deleted = false;
    private java.time.LocalDateTime deletedAt;
    private java.time.LocalDateTime scheduledAt;


    public java.time.LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(java.time.LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public java.time.LocalDateTime getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(java.time.LocalDateTime scheduledAt) {
        this.scheduledAt = scheduledAt;
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

}