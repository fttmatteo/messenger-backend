package app.adapter.out.persistence.entities;

import app.domain.model.enums.PlateType;
import jakarta.persistence.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import java.time.LocalDateTime;

/**
 * Entidad JPA que representa la tabla 'plates'.
 * Cacheable en L2 - Las placas son prácticamente inmutables.
 */
@Entity
@Table(name = "plates")
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY, region = "plates")
public class PlateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_plate")
    private Long idPlate;

    @Column(name = "plate_number", unique = true, nullable = false)
    private String plateNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "plate_type", nullable = false)
    private PlateType plateType;

    @Column(name = "upload_date")
    private LocalDateTime uploadDate;

    public Long getIdPlate() {
        return idPlate;
    }

    public void setIdPlate(Long idPlate) {
        this.idPlate = idPlate;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }

    public PlateType getPlateType() {
        return plateType;
    }

    public void setPlateType(PlateType plateType) {
        this.plateType = plateType;
    }

    public LocalDateTime getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(LocalDateTime uploadDate) {
        this.uploadDate = uploadDate;
    }
}