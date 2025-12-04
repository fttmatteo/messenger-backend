package app.domain.model;

import java.time.LocalDateTime;
import app.domain.model.enums.PlateType;

public class Plate {
    private Long id_plate;
    private String plateNumber;
    private PlateType plateType;
    private LocalDateTime upload_date;

    public Long getId_plate() {
        return id_plate;
    }

    public void setId_plate(Long id_plate) {
        this.id_plate = id_plate;
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

    public LocalDateTime getUpload_date() {
        return upload_date;
    }

    public void setUpload_date(LocalDateTime upload_date) {
        this.upload_date = upload_date;
    }

}
