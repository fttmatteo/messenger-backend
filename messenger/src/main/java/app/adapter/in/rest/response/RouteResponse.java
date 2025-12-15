package app.adapter.in.rest.response;

import java.util.List;

/**
 * DTO (Data Transfer Object) de respuesta para rutas calculadas entre múltiples
 * puntos.
 * 
 * Este objeto proporciona información completa sobre una ruta optimizada,
 * incluyendo origen, destino, puntos intermedios (waypoints), distancia total,
 * duración estimada y la geometría de la ruta para visualización en mapas.
 * 
 * Campos incluidos:
 * - origin: Ubicación de origen
 * - destination: Ubicación de destino final
 * - waypoints: Lista de puntos intermedios (concesionarios a visitar)
 * - distanceMeters: Distancia total en metros
 * - distanceKilometers: Distancia total en kilómetros
 * - durationSeconds: Duración estimada en segundos
 * - durationFormatted: Duración en formato legible
 * - polyline: Geometría de la ruta codificada (para dibujar en Google Maps)
 * 
 * @see app.adapter.in.rest.controllers.MapsController
 * @see app.adapter.out.maps.GoogleMapsAdapter
 */
public class RouteResponse {
    private LocationResponse origin;
    private LocationResponse destination;
    private List<LocationResponse> waypoints;
    private Double distanceMeters;
    private Double distanceKilometers;
    private Long durationSeconds;
    private String durationFormatted;
    private String polyline; // Encoded polyline para dibujar en mapa

    public RouteResponse() {
    }

    public LocationResponse getOrigin() {
        return origin;
    }

    public void setOrigin(LocationResponse origin) {
        this.origin = origin;
    }

    public LocationResponse getDestination() {
        return destination;
    }

    public void setDestination(LocationResponse destination) {
        this.destination = destination;
    }

    public List<LocationResponse> getWaypoints() {
        return waypoints;
    }

    public void setWaypoints(List<LocationResponse> waypoints) {
        this.waypoints = waypoints;
    }

    public Double getDistanceMeters() {
        return distanceMeters;
    }

    public void setDistanceMeters(Double distanceMeters) {
        this.distanceMeters = distanceMeters;
    }

    public Double getDistanceKilometers() {
        return distanceKilometers;
    }

    public void setDistanceKilometers(Double distanceKilometers) {
        this.distanceKilometers = distanceKilometers;
    }

    public Long getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Long durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public String getDurationFormatted() {
        return durationFormatted;
    }

    public void setDurationFormatted(String durationFormatted) {
        this.durationFormatted = durationFormatted;
    }

    public String getPolyline() {
        return polyline;
    }

    public void setPolyline(String polyline) {
        this.polyline = polyline;
    }
}
