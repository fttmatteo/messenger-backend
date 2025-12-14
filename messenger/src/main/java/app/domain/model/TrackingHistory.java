package app.domain.model;

import app.domain.model.enums.TrackingSource;
import java.time.LocalDateTime;

/**
 * Modelo que representa un registro histórico de ubicación.
 * Se almacena en la base de datos para análisis y reportes.
 */
public class TrackingHistory {
    private Long historyId;
    private Long messengerId;
    private Location location;
    private LocalDateTime recordedAt;
    private Long serviceDeliveryId; // Servicio activo en ese momento (puede ser null)
    private TrackingSource source;
    private Double speed; // km/h en el momento del registro

    public TrackingHistory() {
    }

    public TrackingHistory(Long messengerId, Location location, TrackingSource source) {
        this.messengerId = messengerId;
        this.location = location;
        this.recordedAt = LocalDateTime.now();
        this.source = source;
    }

    public Long getHistoryId() {
        return historyId;
    }

    public void setHistoryId(Long historyId) {
        this.historyId = historyId;
    }

    public Long getMessengerId() {
        return messengerId;
    }

    public void setMessengerId(Long messengerId) {
        this.messengerId = messengerId;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public LocalDateTime getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(LocalDateTime recordedAt) {
        this.recordedAt = recordedAt;
    }

    public Long getServiceDeliveryId() {
        return serviceDeliveryId;
    }

    public void setServiceDeliveryId(Long serviceDeliveryId) {
        this.serviceDeliveryId = serviceDeliveryId;
    }

    public TrackingSource getSource() {
        return source;
    }

    public void setSource(TrackingSource source) {
        this.source = source;
    }

    public Double getSpeed() {
        return speed;
    }

    public void setSpeed(Double speed) {
        this.speed = speed;
    }
}
