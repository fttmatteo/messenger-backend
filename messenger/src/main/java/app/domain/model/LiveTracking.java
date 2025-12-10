package app.domain.model;

import app.domain.model.enums.TrackingStatus;
import java.time.LocalDateTime;

/**
 * Modelo que representa el tracking en tiempo real de un mensajero.
 * Se almacena en Redis para acceso rápido.
 */
public class LiveTracking {
    private Long trackingId;
    private Long messengerId;
    private String messengerName;
    private Location currentLocation;
    private LocalDateTime lastUpdate;
    private String deviceId;
    private TrackingStatus status;
    private Double speed; // km/h
    private Double heading; // Dirección en grados (0-360, donde 0 = Norte)

    public LiveTracking() {
    }

    public LiveTracking(Long messengerId, Location currentLocation) {
        this.messengerId = messengerId;
        this.currentLocation = currentLocation;
        this.lastUpdate = LocalDateTime.now();
        this.status = TrackingStatus.ACTIVE;
    }

    /**
     * Verifica si el tracking está activo (última actualización hace menos de X
     * minutos).
     */
    public boolean isActive(int maxMinutesInactive) {
        if (lastUpdate == null) {
            return false;
        }
        return lastUpdate.plusMinutes(maxMinutesInactive).isAfter(LocalDateTime.now());
    }

    /**
     * Obtiene la dirección cardinal basada en el heading.
     */
    public String getCardinalDirection() {
        if (heading == null) {
            return "Desconocido";
        }
        if (heading >= 337.5 || heading < 22.5)
            return "Norte";
        if (heading >= 22.5 && heading < 67.5)
            return "Noreste";
        if (heading >= 67.5 && heading < 112.5)
            return "Este";
        if (heading >= 112.5 && heading < 157.5)
            return "Sureste";
        if (heading >= 157.5 && heading < 202.5)
            return "Sur";
        if (heading >= 202.5 && heading < 247.5)
            return "Suroeste";
        if (heading >= 247.5 && heading < 292.5)
            return "Oeste";
        return "Noroeste";
    }

    // Getters y Setters
    public Long getTrackingId() {
        return trackingId;
    }

    public void setTrackingId(Long trackingId) {
        this.trackingId = trackingId;
    }

    public Long getMessengerId() {
        return messengerId;
    }

    public void setMessengerId(Long messengerId) {
        this.messengerId = messengerId;
    }

    public String getMessengerName() {
        return messengerName;
    }

    public void setMessengerName(String messengerName) {
        this.messengerName = messengerName;
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(Location currentLocation) {
        this.currentLocation = currentLocation;
    }

    public LocalDateTime getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(LocalDateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public TrackingStatus getStatus() {
        return status;
    }

    public void setStatus(TrackingStatus status) {
        this.status = status;
    }

    public Double getSpeed() {
        return speed;
    }

    public void setSpeed(Double speed) {
        this.speed = speed;
    }

    public Double getHeading() {
        return heading;
    }

    public void setHeading(Double heading) {
        this.heading = heading;
    }
}
