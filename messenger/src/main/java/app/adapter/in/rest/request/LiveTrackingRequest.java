package app.adapter.in.rest.request;

import app.domain.model.enums.TrackingStatus;

/**
 * DTO (Data Transfer Object) para recibir actualizaciones de ubicación en
 * tiempo real del mensajero.
 * 
 * Este objeto encapsula toda la información de geolocalización y estado del
 * mensajero,
 * permitiendo el seguimiento en tiempo real durante la ejecución de servicios
 * de entrega.
 * 
 * Información capturada:
 * - messengerId: Identificador único del mensajero
 * - latitude/longitude: Coordenadas geográficas actuales
 * - accuracy: Precisión de la ubicación en metros
 * - speed: Velocidad actual en m/s
 * - heading: Dirección del movimiento (0-360°, 0=Norte)
 * - status: Estado del rastreo (ACTIVE, INACTIVE, etc.)
 * - deviceId: Identificador único del dispositivo móvil
 * 
 * Los datos se almacenan en el historial de rastreo y se transmiten en tiempo
 * real
 * a través de WebSockets a los clientes suscritos.
 * 
 * @see app.adapter.in.rest.controllers.TrackingController
 * @see app.adapter.in.websocket.TrackingWebSocketController
 * @see app.domain.model.LiveTracking
 */
public class LiveTrackingRequest {
    private Long messengerId;
    private Double latitude;
    private Double longitude;
    private Double accuracy;
    private Double speed;
    private Double heading;
    private TrackingStatus status;
    private String deviceId;

    public LiveTrackingRequest() {
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

    public Double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(Double accuracy) {
        this.accuracy = accuracy;
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

    public TrackingStatus getStatus() {
        return status;
    }

    public void setStatus(TrackingStatus status) {
        this.status = status;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}
