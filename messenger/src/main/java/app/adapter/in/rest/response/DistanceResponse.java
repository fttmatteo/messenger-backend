package app.adapter.in.rest.response;

/**
 * DTO (Data Transfer Object) de respuesta para cálculos de distancia y duración
 * de viaje.
 * 
 * Este objeto proporciona información detallada sobre la distancia entre dos
 * puntos
 * y el tiempo estimado de viaje, utilizando datos de Google Maps Distance
 * Matrix API.
 * 
 * Campos incluidos:
 * - distanceMeters: Distancia en metros
 * - distanceKilometers: Distancia en kilómetros (calculada automáticamente)
 * - durationSeconds: Duración del viaje en segundos
 * - durationFormatted: Duración formateada en formato legible (ej. "2 hora(s)
 * 30 minuto(s)")
 * 
 * @see app.adapter.in.rest.controllers.MapsController
 * @see app.adapter.out.maps.GoogleMapsAdapter
 */
public class DistanceResponse {
    private Double distanceMeters;
    private Double distanceKilometers;
    private Long durationSeconds;
    private String durationFormatted;

    public DistanceResponse() {
    }

    public DistanceResponse(Double distanceMeters, Long durationSeconds) {
        this.distanceMeters = distanceMeters;
        this.distanceKilometers = distanceMeters != null ? distanceMeters / 1000.0 : null;
        this.durationSeconds = durationSeconds;
        if (durationSeconds != null) {
            long hours = durationSeconds / 3600;
            long minutes = (durationSeconds % 3600) / 60;
            this.durationFormatted = hours > 0
                    ? String.format("%d hora(s) %d minuto(s)", hours, minutes)
                    : String.format("%d minuto(s)", minutes);
        }
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
}
