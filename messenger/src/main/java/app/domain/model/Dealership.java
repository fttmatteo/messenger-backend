package app.domain.model;

import app.domain.model.enums.Zone;

public class Dealership {
    private Long id_dealership;
    private String name;
    private String address;
    private String phone;
    private Zone zone;

    public Long getId_dealership() {
        return id_dealership;
    }

    public void setId_dealership(Long id_dealership) {
        this.id_dealership = id_dealership;
    }

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

    public Zone getZone() {
        return zone;
    }

    public void setZone(Zone zone) {
        this.zone = zone;
    }
}
