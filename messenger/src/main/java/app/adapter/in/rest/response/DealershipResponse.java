package app.adapter.in.rest.response;

public class DealershipResponse {
    private Long idDealership;
    private String name;
    private String address;
    private String phone;
    private String zone;

    public DealershipResponse() {
    }

    public DealershipResponse(Long idDealership, String name, String address, String phone, String zone) {
        this.idDealership = idDealership;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.zone = zone;
    }

    public Long getIdDealership() {
        return idDealership;
    }

    public void setIdDealership(Long idDealership) {
        this.idDealership = idDealership;
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

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }
}
