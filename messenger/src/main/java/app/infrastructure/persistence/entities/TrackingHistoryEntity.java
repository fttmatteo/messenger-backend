package app.infrastructure.persistence.entities;

import app.domain.model.enums.TrackingSource;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad JPA para almacenar historial de ubicaciones de tracking.
 * 
 * Almacena todas las ubicaciones GPS de los mensajeros durante sus entregas
 * para análisis de rutas, reportes y optimización de tiempos.
 * 
 * Índices:
 * - idx_messenger_date: Optimiza consultas por mensajero y fecha
 * - idx_service: Optimiza consultas por servicio de entrega
 */
@Entity
@Table(name = "tracking_history", indexes = {
        @Index(name = "idx_messenger_date", columnList = "messenger_id,recorded_at"),
        @Index(name = "idx_service", columnList = "service_delivery_id")
})
public class TrackingHistoryEntity {

    /** Identificador único del registro de tracking (clave primaria). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId;

    /** ID del mensajero cuya ubicación se está registrando. */
    @Column(name = "messenger_id", nullable = false)
    private Long messengerId;

    /** Latitud de la ubicación GPS del mensajero. */
    @Column(name = "latitude", nullable = false)
    private Double latitude;

    /** Longitud de la ubicación GPS del mensajero. */
    @Column(name = "longitude", nullable = false)
    private Double longitude;

    /** Fecha y hora en que se registró esta ubicación. */
    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    /**
     * ID del servicio de entrega asociado (opcional, puede ser tracking sin
     * servicio activo).
     */
    @Column(name = "service_delivery_id")
    private Long serviceDeliveryId;

    /** Fuente del registro de tracking (WEBSOCKET, MANUAL, AUTOMATIC). */
    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false)
    private TrackingSource source;

    /** Velocidad del mensajero en el momento del registro (km/h, opcional). */
    @Column(name = "speed")
    private Double speed;

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
