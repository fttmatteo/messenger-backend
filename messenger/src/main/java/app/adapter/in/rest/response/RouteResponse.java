package app.adapter.in.rest.response;

import java.util.List;

/**
 * DTO de respuesta para rutas calculadas.
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
