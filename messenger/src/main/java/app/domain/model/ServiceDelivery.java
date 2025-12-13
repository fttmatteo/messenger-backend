package app.domain.model;

import app.domain.model.enums.Status;

import java.util.ArrayList;
import java.util.List;

/**
 * Modelo de dominio que representa un servicio de entrega de placas
 * vehiculares.
 * 
 * <p>
 * Esta clase encapsula toda la información relacionada con la entrega de una
 * placa
 * a un concesionario, incluyendo:
 * </p>
 * <ul>
 * <li>Información de la placa vehicular</li>
 * <li>Concesionario destino</li>
 * <li>Mensajero asignado</li>
 * <li>Estado actual del servicio (PENDING, DELIVERED, RETURNED, etc.)</li>
 * <li>Evidencias: firma digital y fotografías</li>
 * <li>Historial completo de cambios de estado</li>
 * </ul>
 * 
 * @see Status
 * @see Plate
 * @see Dealership
 * @see Employee
 */
public class ServiceDelivery {
    private Long idServiceDelivery;
    private Plate plate;
    private Dealership dealership;
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

    public java.time.LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(java.time.LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}