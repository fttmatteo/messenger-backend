package app.infrastructure.persistence.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "dealerships")
public class DealershipEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idDealership;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, length = 100)
    private String address;

    @Column(nullable = false, length = 10)
    private String phone;

    @Column(nullable = false, length = 10)
    private String zone;

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