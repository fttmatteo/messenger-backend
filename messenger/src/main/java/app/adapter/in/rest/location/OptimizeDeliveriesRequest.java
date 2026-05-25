package app.adapter.in.rest.location;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * DTO para solicitar la optimización de múltiples servicios de entrega.
 */
public class OptimizeDeliveriesRequest {

    @NotNull(message = "La latitud actual es obligatoria")
    private Double currentLatitude;

    @NotNull(message = "La longitud actual es obligatoria")
    private Double currentLongitude;

    @NotEmpty(message = "La lista de UUIDs de servicios es obligatoria")
    private List<String> serviceUuids;

    public OptimizeDeliveriesRequest() {
    }

    public Double getCurrentLatitude() {
        return currentLatitude;
    }

    public void setCurrentLatitude(Double currentLatitude) {
        this.currentLatitude = currentLatitude;
    }

    public Double getCurrentLongitude() {
        return currentLongitude;
    }

    public void setCurrentLongitude(Double currentLongitude) {
        this.currentLongitude = currentLongitude;
    }

    public List<String> getServiceUuids() {
        return serviceUuids;
    }

    public void setServiceUuids(List<String> serviceUuids) {
        this.serviceUuids = serviceUuids;
    }
}
