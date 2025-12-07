// Archivo: app/domain/ports/PlatePort.java
package app.domain.ports;

import app.domain.model.Plate;
import java.util.List;

public interface PlatePort {
    Plate findById(Long idPlate);
    Plate findByPlateNumber(String plateNumber);
    List<Plate> findAll();
}