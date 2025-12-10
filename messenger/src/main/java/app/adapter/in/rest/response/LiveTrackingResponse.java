package app.adapter.in.rest.response;

import app.domain.model.enums.TrackingStatus;
import java.time.LocalDateTime;

/**
 * DTO de respuesta para tracking en tiempo real de un mensajero.
 */
public class LiveTrackingResponse {
    private Long messengerId;
    private String messengerName;
    private Double latitude;
    private Double longitude;
    private LocalDateTime lastUpdate;
    private TrackingStatus status;
    private Double speed;
    private Double heading;

    public LiveTrackingResponse() {
    }

    public LiveTrackingResponse(Long messengerId, String messengerName,
            Double latitude, Double longitude,
            LocalDateTime lastUpdate, TrackingStatus status,
            Double speed, Double heading) {
        this.messengerId = messengerId;
        this.messengerName = messengerName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.lastUpdate = lastUpdate;
        this.status = status;
        this.speed = speed;
        this.heading = heading;
    }

    // Getters y Setters
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

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public LocalDateTime getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(LocalDateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
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
