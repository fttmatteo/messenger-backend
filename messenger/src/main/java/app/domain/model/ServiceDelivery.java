package app.domain.model;

import app.domain.model.enums.Status;
import java.util.List;

public class ServiceDelivery {
    private Long idServiceDelivery;
    private Plate plate;
    private Dealership dealership;
    private Employee messenger;
    private Status currentStatus;
    private String observation;
    private Signature signature;
    private List<Photo> photos = new ArrayList<>(); // Inicializamos las listas para evitar NullPointerException
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
}