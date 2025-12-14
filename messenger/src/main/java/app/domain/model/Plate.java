package app.domain.model;

import java.time.LocalDateTime;
import app.domain.model.enums.PlateType;

/**
 * Modelo de dominio que representa una placa vehicular reconocida mediante OCR.
 * 
 * Esta clase almacena la información de una placa detectada a partir de una
 * imagen
 * utilizando tecnología de reconocimiento óptico de caracteres (Google Cloud
 * Vision API).
 * 
 * Tipos de placas soportados:
 * MOTORCYCLE: Formato ABC 12A
 * CAR: Formato ABC 123
 * MOTORBIKE: Formato 123 ABC
 */
public class Plate {
    private Long idPlate;
    private String plateNumber;
    private PlateType plateType;
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
