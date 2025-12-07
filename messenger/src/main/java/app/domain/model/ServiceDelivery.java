package app.domain.model;

import app.domain.model.enums.Status;
import java.util.List;

public class ServiceDelivery {
    private Long idServiceDelivery;
    private Plate plate;
    private Dealership dealership;
    private Employee messenger; // El mensajero asignado
    private Status currentStatus;
    // Observación actual (requerida para estados fallidos, opcional para entregado)
    private String observation;
    // Relaciones para evidencias actuales
    private Signature signature;
    // Inicializamos las listas para evitar NullPointerException
    private List<Photo> photos = new ArrayList<>();
    // Historial completo para auditoría
    private List<StatusHistory> history = new ArrayList<>();
    // Métodos utilitarios para mantener encapsulamiento (opcional pero recomendado)
    
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
}