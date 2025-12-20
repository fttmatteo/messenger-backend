package app.domain.model;

public class Dealership {
    private Long idDealership;
    private String name;
    private String address;
    private String phone;
    private String zone;
    private Double latitude;
    private Double longitude;
    private Boolean isGeolocated = false;

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

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Boolean getIsGeolocated() {
        return isGeolocated;
    }

    public void setIsGeolocated(Boolean isGeolocated) {
        this.isGeolocated = isGeolocated;
    }

    /**
     * Convierte los campos de lat/lng en un objeto Location.
     * 
     * @return Location o null si no está geocodificado
     */
    public Location getLocation() {
        if (latitude == null || longitude == null) {
            return null;
        }
        return new Location(latitude, longitude);
    }

    /**
     * Establece la ubicación desde un objeto Location.
     */
    public void setLocation(Location location) {
        if (location != null) {
            this.latitude = location.getLatitude();
            this.longitude = location.getLongitude();
            this.isGeolocated = true;
        }
    }
}
