package app.adapter.in.rest.request;

import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * DTO (Data Transfer Object) para solicitudes de cálculo de rutas optimizadas.
 * 
 * Este objeto se utiliza para calcular la ruta óptima desde un punto de origen
 * hacia múltiples concesionarios (destinos), utilizando la API de Google Maps
 * Directions.
 * 
 * Parámetros de la ruta:
 * - originLatitude/originLongitude: Coordenadas del punto de partida
 * - dealershipIds: Lista de IDs de concesionarios a visitar
 * - optimize: Si es true, optimiza el orden de las paradas; si es false,
 * respeta el orden de la lista
 * 
 * El servicio retorna información detallada de la ruta incluyendo distancia
 * total,
 * duración estimada, y el orden optimizado de las paradas.
 * 
 * @see app.adapter.in.rest.controllers.MapsController
 * @see app.adapter.out.maps.GoogleMapsAdapter
 */
public class RouteRequest {

    @NotNull(message = "La latitud de origen es obligatoria")
    private Double originLatitude;

    @NotNull(message = "La longitud de origen es obligatoria")
    private Double originLongitude;

    @NotNull(message = "Los destinos son obligatorios")
    private List<Long> dealershipIds;

    private Boolean optimize = true;

    public RouteRequest() {
    }

    public Double getOriginLatitude() {
        return originLatitude;
    }

    public void setOriginLatitude(Double originLatitude) {
        this.originLatitude = originLatitude;
    }

    public Double getOriginLongitude() {
        return originLongitude;
    }

    public void setOriginLongitude(Double originLongitude) {
        this.originLongitude = originLongitude;
    }

    public List<Long> getDealershipIds() {
        return dealershipIds;
    }

    public void setDealershipIds(List<Long> dealershipIds) {
        this.dealershipIds = dealershipIds;
    }

    public Boolean getOptimize() {
        return optimize;
    }

    public void setOptimize(Boolean optimize) {
        this.optimize = optimize;
    }
}
