package app.adapter.in.rest.response;

/**
 * DTO de respuesta para geocodificación.
 */
public class LocationResponse {
    private Double latitude;
    private Double longitude;
    private String formattedAddress;

    public LocationResponse() {
    }

    public LocationResponse(Double latitude, Double longitude, String formattedAddress) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.formattedAddress = formattedAddress;
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

    public String getFormattedAddress() {
        return formattedAddress;
    }

    public void setFormattedAddress(String formattedAddress) {
        this.formattedAddress = formattedAddress;
    }
}
