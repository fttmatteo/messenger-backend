package app.adapter.in.rest.request;

import jakarta.validation.constraints.NotBlank;

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
