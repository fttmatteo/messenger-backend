package app.adapter.in.rest.request;

/**
 * DTO (Data Transfer Object) para las peticiones de creación y actualización de
 * concesionarios.
 * 
 * Este objeto encapsula la información básica necesaria para registrar o
 * modificar
 * un concesionario en el sistema, incluyendo datos de identificación, ubicación
 * y contacto.
 * 
 * Campos incluidos:
 * - name: Nombre comercial del concesionario
 * - address: Dirección física completa
 * - phone: Número de teléfono de contacto
 * - zone: Zona geográfica o administrativa de operación
 * 
 * @see app.adapter.in.rest.controllers.DealershipController
 * @see app.domain.model.Dealership
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
