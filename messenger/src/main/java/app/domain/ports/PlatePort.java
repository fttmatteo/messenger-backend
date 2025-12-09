package app.domain.ports;

import app.domain.model.Plate;
import java.util.List;

public interface PlatePort {
    void save(Plate plate);

    Plate findById(Long idPlate);

    Plate findByPlateNumber(String plateNumber);

    List<Plate> findAll();
}