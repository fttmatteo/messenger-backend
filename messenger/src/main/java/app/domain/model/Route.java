package app.domain.model;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Value Object inmutable que representa una ruta entre dos ubicaciones.
 * Contiene información de distancia, duración y puntos intermedios.
 */
public class Route {
    private final Location origin;
    private final Location destination;
    private final List<Location> waypoints; // Puntos intermedios de paso
    private final Double distanceMeters;
    private final Long durationSeconds;
    private final String polyline; // Geometría codificada de la ruta

    public Route(Location origin, Location destination, List<Location> waypoints,
            Double distanceMeters, Long durationSeconds, String polyline) {
        if (origin == null || destination == null) {
            throw new IllegalArgumentException("Origen y destino no pueden ser nulos");
        }
        this.origin = origin;
        this.destination = destination;
        this.waypoints = waypoints != null ? new ArrayList<>(waypoints) : new ArrayList<>();
        this.distanceMeters = distanceMeters;
        this.durationSeconds = durationSeconds;
        this.polyline = polyline;
    }

    public Double getDistanceKilometers() {
        return distanceMeters != null ? distanceMeters / 1000.0 : null;
    }

    public boolean hasWaypoints() {
        return waypoints != null && !waypoints.isEmpty();
    }

    public int getTotalStops() {
        return 2 + (waypoints != null ? waypoints.size() : 0);
    }

    public Location getOrigin() {
        return origin;
    }

    public Location getDestination() {
        return destination;
    }

    public List<Location> getWaypoints() {
        return Collections.unmodifiableList(waypoints);
    }

    public Double getDistanceMeters() {
        return distanceMeters;
    }

    public Long getDurationSeconds() {
        return durationSeconds;
    }

    public String getPolyline() {
        return polyline;
    }

    @Override
    public String toString() {
        return String.format("Route[from=%s, to=%s, distance=%.2fkm, durationSec=%d]",
                origin, destination, getDistanceKilometers(), durationSeconds);
    }
}
