package app.adapter.in.rest.request;

import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * DTO para solicitar cálculo de ruta.
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
