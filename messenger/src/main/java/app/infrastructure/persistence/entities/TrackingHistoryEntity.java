package app.infrastructure.persistence.entities;

import app.domain.model.enums.TrackingSource;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad JPA para almacenar historial de ubicaciones de tracking.
 * Almacena todas las ubicaciones de los mensajeros para análisis y reportes.
 */
@Entity
@Table(name = "tracking_history", indexes = {
        @Index(name = "idx_messenger_date", columnList = "messenger_id,recorded_at"),
        @Index(name = "idx_service", columnList = "service_delivery_id")
})
public class TrackingHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId;

    @Column(name = "messenger_id", nullable = false)
    private Long messengerId;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    @Column(name = "service_delivery_id")
    private Long serviceDeliveryId;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false)
    private TrackingSource source;

    @Column(name = "speed")
    private Double speed;

    // Getters y Setters
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
