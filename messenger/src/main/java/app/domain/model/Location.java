package app.domain.model;

import java.time.LocalDateTime;

/**
 * Value Object inmutable que representa una ubicación geográfica.
 * Incluye validaciones y cálculo de distancias usando fórmula Haversine.
 */
public class Location {
    private final Double latitude;
    private final Double longitude;
    private final LocalDateTime timestamp;
    private final Double accuracy; // Precisión en metros

    // Radio de la Tierra en metros
    private static final double EARTH_RADIUS_METERS = 6371000;

    public Location(Double latitude, Double longitude) {
        this(latitude, longitude, null, null);
    }

    public Location(Double latitude, Double longitude, LocalDateTime timestamp, Double accuracy) {
        validateCoordinates(latitude, longitude);
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
        this.accuracy = accuracy;
    }

    private void validateCoordinates(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            throw new IllegalArgumentException("Las coordenadas no pueden ser nulas");
        }
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("La latitud debe estar entre -90 y 90 grados");
        }
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("La longitud debe estar entre -180 y 180 grados");
        }
    }

    /**
     * Calcula la distancia en metros a otra ubicación usando la fórmula Haversine.
     * 
     * @param other La otra ubicación
     * @return Distancia en metros
     */
    public Double distanceTo(Location other) {
        if (other == null) {
            return null;
        }

        double lat1Rad = Math.toRadians(this.latitude);
        double lat2Rad = Math.toRadians(other.latitude);
        double deltaLatRad = Math.toRadians(other.latitude - this.latitude);
        double deltaLngRad = Math.toRadians(other.longitude - this.longitude);

        double a = Math.sin(deltaLatRad / 2) * Math.sin(deltaLatRad / 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                        Math.sin(deltaLngRad / 2) * Math.sin(deltaLngRad / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_METERS * c;
    }

    /**
     * Verifica si esta ubicación está dentro de un radio específico de otra
     * ubicación.
     * 
     * @param center       Ubicación central
     * @param radiusMeters Radio en metros
     * @return true si está dentro del radio
     */
    public boolean isWithinRadius(Location center, Double radiusMeters) {
        if (center == null || radiusMeters == null) {
            return false;
        }
        Double distance = distanceTo(center);
        return distance != null && distance <= radiusMeters;
    }

    /**
     * Verifica si las coordenadas son válidas.
     */
    public boolean isValid() {
        return latitude != null && longitude != null &&
                latitude >= -90 && latitude <= 90 &&
                longitude >= -180 && longitude <= 180;
    }

    // Getters (inmutables, sin setters)
    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public Double getAccuracy() {
        return accuracy;
    }

    @Override
    public String toString() {
        return String.format("Location[lat=%.6f, lng=%.6f]", latitude, longitude);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Location location = (Location) obj;
        return Double.compare(location.latitude, latitude) == 0 &&
                Double.compare(location.longitude, longitude) == 0;
    }

    @Override
    public int hashCode() {
        int result = latitude.hashCode();
        result = 31 * result + longitude.hashCode();
        return result;
    }
}
