package app.adapter.in.rest.response;

/**
 * DTO de respuesta para cálculo de distancia.
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
