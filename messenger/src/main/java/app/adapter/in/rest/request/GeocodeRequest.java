package app.adapter.in.rest.request;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO (Data Transfer Object) para solicitudes de geocodificación de
 * direcciones.
 * 
 * Este objeto se utiliza para enviar una dirección en formato de texto libre
 * al servicio de geocodificación, que retornará las coordenadas geográficas
 * (latitud y longitud) correspondientes.
 * 
 * La dirección debe ser lo más específica posible para obtener resultados
 * precisos.
 * Se utiliza la API de Google Maps Geocoding para realizar la conversión.
 * 
 * @see app.adapter.in.rest.controllers.MapsController
 * @see app.adapter.out.maps.GoogleMapsAdapter
 */
public class GeocodeRequest {

    @NotBlank(message = "La dirección es obligatoria")
    private String address;

    public GeocodeRequest() {
    }

    public GeocodeRequest(String address) {
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
