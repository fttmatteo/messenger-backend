package app.domain.model;

public class Concessionarie {
    private Long id_concesionarie;
    private String name;
    private String address;
    private String phone;

    public Long getId_concesionarie() {
        return id_concesionarie;
    }

    public void setId_concesionarie(Long id_concesionarie) {
        this.id_concesionarie = id_concesionarie;
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
}
