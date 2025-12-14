package app.infrastructure.persistence.entities;

import app.domain.model.enums.PlateType;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad JPA que representa la tabla 'plates'.
 * 
 * Almacena las placas vehiculares reconocidas mediante OCR y su tipo
 * determinado automáticamente por el sistema.
 * 
 * Relaciones:
 * - Una placa puede estar asociada a múltiples ServiceDelivery
 */
@Entity
@Table(name = "plates")
public class PlateEntity {

    /** Identificador único de la placa (clave primaria). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_plate")
    private Long idPlate;

    /** Número de placa vehicular (único, formato normalizado: "ABC 123"). */
    @Column(name = "plate_number", unique = true, nullable = false)
    private String plateNumber;

    /**
     * Tipo de vehículo determinado por el formato de la placa (CAR, MOTORCYCLE,
     * MOTORCAR).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "plate_type", nullable = false)
    private PlateType plateType;

    /**
     * Fecha y hora en que la placa fue registrada por primera vez en el sistema.
     */
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