package app.adapter.in.rest.response;

import app.domain.model.enums.TrackingStatus;
import java.time.LocalDateTime;

/**
 * DTO (Data Transfer Object) de respuesta para el seguimiento en tiempo real de
 * un mensajero.
 * 
 * Este objeto proporciona la ubicación actual y datos de movimiento del
 * mensajero,
 * utilizado para visualización en mapas y monitoreo de entregas en tiempo real.
 * 
 * Campos incluidos:
 * - messengerId: Identificador único del mensajero
 * - messengerName: Nombre completo del mensajero
 * - latitude/longitude: Coordenadas geográficas actuales
 * - lastUpdate: Fecha y hora de la última actualización de ubicación
 * - status: Estado actual del rastreo (ACTIVE, INACTIVE, etc.)
 * - speed: Velocidad actual en m/s
 * - heading: Dirección del movimiento en grados (0-360°, 0=Norte)
 * 
 * Los datos se actualizan en tiempo real a través de WebSockets.
 * 
 * @see app.adapter.in.rest.controllers.TrackingController
 * @see app.adapter.in.websocket.TrackingWebSocketController
 * @see app.domain.model.LiveTracking
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
