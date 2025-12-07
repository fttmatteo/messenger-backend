package app.domain.ports;

import app.domain.model.Plate;

public interface PlatePort {
    void save(Plate plate) throws Exception;

    void deleteById(Long idPlate) throws Exception;

    Plate findById(Long idPlate) throws Exception;

    Plate findByPlateNumber(String plateNumber) throws Exception;
}