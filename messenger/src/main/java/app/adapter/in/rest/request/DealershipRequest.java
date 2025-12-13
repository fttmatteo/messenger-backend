package app.adapter.in.rest.request;

/**
 * DTO para crear o actualizar un concesionario.
 * 
 * <p>
 * Contiene la información básica del concesionario: nombre, dirección, teléfono
 * y zona.
 * </p>
 */
public class DealershipRequest {
    private String name;
    private String address;
    private String phone;
    private String zone;

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
}
