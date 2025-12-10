package app.domain.model;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Value Object que representa una ruta calculada entre ubicaciones.
 * Incluye información sobre distancia, duración y waypoints.
 */
public class Route {
    private final Location origin;
    private final Location destination;
    private final List<Location> waypoints;
    private final Double distanceMeters;
    private final Long durationSeconds;
    private final String polyline; // Encoded polyline de Google para dibujar en mapa

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

    /**
     * Obtiene la distancia en kilómetros.
     */
    public Double getDistanceKilometers() {
        return distanceMeters != null ? distanceMeters / 1000.0 : null;
    }

    /**
     * Obtiene la duración formateada como "X horas Y minutos".
     */
    public String getDurationFormatted() {
        if (durationSeconds == null) {
            return null;
        }
        long hours = durationSeconds / 3600;
        long minutes = (durationSeconds % 3600) / 60;

        if (hours > 0) {
            return String.format("%d hora(s) %d minuto(s)", hours, minutes);
        } else {
            return String.format("%d minuto(s)", minutes);
        }
    }

    /**
     * Verifica si la ruta tiene waypoints intermedios.
     */
    public boolean hasWaypoints() {
        return waypoints != null && !waypoints.isEmpty();
    }

    /**
     * Obtiene el número total de paradas (incluyendo origen y destino).
     */
    public int getTotalStops() {
        return 2 + (waypoints != null ? waypoints.size() : 0);
    }

    // Getters (inmutables)
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
        return String.format("Route[from=%s, to=%s, distance=%.2fkm, duration=%s]",
                origin, destination, getDistanceKilometers(), getDurationFormatted());
    }
}
