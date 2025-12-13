package app.domain.ports;

import app.domain.model.Plate;
import java.util.List;

/**
 * Puerto (interfaz) para operaciones de persistencia de placas vehiculares.
 * 
 * <p>
 * Define las operaciones para almacenar y consultar placas detectadas por OCR.
 * </p>
 */
public interface PlatePort {
    void save(Plate plate);

    Plate findById(Long idPlate);

    Plate findByPlateNumber(String plateNumber);

    List<Plate> findAll();
}