package app.domain.ports;

import app.domain.model.Plate;
import java.util.List;

public interface PlatePort {
    Plate save(Plate plate);

    Plate update(Plate plate);

    void deleteById(Long idPlate);

    Plate findById(Long idPlate);

    List<Plate> findAll();

    Plate findByPlateNumber(String plateNumber);
}