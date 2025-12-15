package app.adapter.in.rest.response;

/**
 * DTO (Data Transfer Object) de respuesta para ubicaciones geocodificadas.
 * 
 * Este objeto representa una ubicación geográfica con sus coordenadas y
 * dirección formateada,
 * resultado de procesos de geocodificación o geocodificación inversa.
 * 
 * Campos incluidos:
 * - latitude: Latitud de la ubicación
 * - longitude: Longitud de la ubicación
 * - formattedAddress: Dirección en formato legible proporcionada por Google
 * Maps
 * 
 * @see app.adapter.in.rest.controllers.MapsController
 * @see app.adapter.out.maps.GoogleMapsAdapter
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
