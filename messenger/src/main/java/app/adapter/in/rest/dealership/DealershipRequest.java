package app.adapter.in.rest.dealership;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO para crear o actualizar concesionarios.
 */
public class DealershipRequest {
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede tener más de 100 caracteres")
    private String name;

    @NotBlank(message = "La dirección es obligatoria")
    private String address;

    @NotBlank(message = "El teléfono es obligatorio")
    @Size(min = 7, message = "El teléfono debe tener al menos 7 dígitos")
    @Pattern(regexp = "^\\d+$", message = "El teléfono solo debe contener números")
    private String phone;

    @NotBlank(message = "La zona es obligatoria")
    private String zone;

    private String whatsappPin;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public String getWhatsappPin() {
        return whatsappPin;
    }

    public void setWhatsappPin(String whatsappPin) {
        this.whatsappPin = whatsappPin;
    }
}
