package app.adapter.in.rest.response;

import java.time.LocalDateTime;

/**
 * DTO (Data Transfer Object) de respuesta para registros históricos de rastreo
 * de mensajeros.
 * 
 * Este objeto representa un punto en el historial de ubicaciones de un
 * mensajero,
 * permitiendo reconstruir la ruta completa seguida durante un servicio de
 * entrega.
 * 
 * Campos incluidos:
 * - historyId: Identificador único del registro histórico
 * - messengerId: Identificador del mensajero rastreado
 * - latitude/longitude: Coordenadas geográficas registradas
 * - recordedAt: Fecha y hora del registro de ubicación
 * - serviceDeliveryId: ID del servicio asociado (si aplica)
 * - source: Origen del dato (GPS, NETWORK, etc.)
 * - speed: Velocidad registrada en m/s
 * 
 * Los registros históricos se almacenan para análisis posterior y visualización
 * de rutas.
 * 
 * @see app.adapter.in.rest.controllers.TrackingController
 * @see app.domain.model.TrackingHistory
 */
public class TrackingHistoryResponse {
    private Long historyId;
    private Long messengerId;
    private Double latitude;
    private Double longitude;
    private LocalDateTime recordedAt;
    private Long serviceDeliveryId;
    private String source;
    private Double speed;

    public TrackingHistoryResponse() {
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

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Double getSpeed() {
        return speed;
    }

    public void setSpeed(Double speed) {
        this.speed = speed;
    }
}
