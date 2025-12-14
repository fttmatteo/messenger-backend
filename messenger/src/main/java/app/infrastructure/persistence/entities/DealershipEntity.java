package app.infrastructure.persistence.entities;

import jakarta.persistence.*;

/**
 * Entidad JPA que representa la tabla 'dealerships'.
 * 
 * Almacena la información de los concesionarios incluyendo su ubicación
 * geográfica obtenida mediante geocodificación con Google Maps API.
 * 
 * Relaciones:
 * - Un concesionario puede tener múltiples ServiceDelivery
 */
@Entity
@Table(name = "dealerships")
public class DealershipEntity {
    /** Identificador único del concesionario (clave primaria). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idDealership;

    /** Nombre del concesionario (único en el sistema). */
    @Column(nullable = false, unique = true)
    private String name;

    /** Dirección física del concesionario. */
    @Column(nullable = false, length = 100)
    private String address;

    /** Número de teléfono de contacto. */
    @Column(nullable = false, length = 10)
    private String phone;

    /** Zona geográfica donde opera el concesionario. */
    @Column(nullable = false, length = 10)
    private String zone;

    /** Latitud de la ubicación del concesionario (obtenida por geocodificación). */
    @Column(name = "latitude")
    private Double latitude;

    /**
     * Longitud de la ubicación del concesionario (obtenida por geocodificación).
     */
    @Column(name = "longitude")
    private Double longitude;

    /** Indica si el concesionario ha sido geocodificado exitosamente. */
    @Column(name = "is_geolocated")
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
}