package app.infrastructure.persistence.entities;

import app.domain.model.enums.TrackingSource;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad JPA para historial de tracking archivado.
 */
@Entity
@Table(name = "deleted_tracking_history")
public class DeletedTrackingHistoryEntity {

    @Id
    @Column(name = "history_id")
    private Long historyId;

    @Column(name = "service_delivery_id", nullable = false)
    private Long serviceDeliveryId;

    @Column(name = "messenger_id", nullable = false)
    private Long messengerId;

    @Column(nullable = false, precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 11, scale = 8)
    private BigDecimal longitude;

    @Column(precision = 5, scale = 2)
    private BigDecimal speed;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TrackingSource source;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    public DeletedTrackingHistoryEntity() {
    }

    public Long getHistoryId() {
        return historyId;
    }

    public void setHistoryId(Long historyId) {
        this.historyId = historyId;
    }

    public Long getServiceDeliveryId() {
        return serviceDeliveryId;
    }

    public void setServiceDeliveryId(Long serviceDeliveryId) {
        this.serviceDeliveryId = serviceDeliveryId;
    }

    public Long getMessengerId() {
        return messengerId;
    }

    public void setMessengerId(Long messengerId) {
        this.messengerId = messengerId;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public BigDecimal getSpeed() {
        return speed;
    }

    public void setSpeed(BigDecimal speed) {
        this.speed = speed;
    }

    public TrackingSource getSource() {
        return source;
    }

    public void setSource(TrackingSource source) {
        this.source = source;
    }

    public LocalDateTime getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(LocalDateTime recordedAt) {
        this.recordedAt = recordedAt;
    }
}
