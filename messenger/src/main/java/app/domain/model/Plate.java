package app.domain.model;

import java.time.LocalDateTime;
import app.domain.model.enums.PlateType;

public class Plate {
    private Long id_plate;
    private String plate_number;
    private PlateType plate_type;
    private LocalDateTime upload_date;

    public Long getId_plate() {
        return id_plate;
    }

    public void setId_plate(Long id_plate) {
        this.id_plate = id_plate;
    }

    public String getPlate_number() {
        return plate_number;
    }

    public void setPlate_number(String plate_number) {
        this.plate_number = plate_number;
    }

    public PlateType getPlate_type() {
        return plate_type;
    }

    public void setPlate_type(PlateType plate_type) {
        this.plate_type = plate_type;
    }

    public LocalDateTime getUpload_date() {
        return upload_date;
    }

    public void setUpload_date(LocalDateTime upload_date) {
        this.upload_date = upload_date;
    }

}
