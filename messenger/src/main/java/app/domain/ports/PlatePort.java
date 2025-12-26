package app.domain.ports;

import app.domain.model.Plate;
import java.util.List;

/**
 * Puerto de salida para persistencia de placas vehiculares.
 */
public interface PlatePort {

    /**
     * Guarda o actualiza una placa.
     */
    void save(Plate plate);

    /**
     * Busca una placa por su ID.
     */
    Plate findById(Long idPlate);

    /**
     * Busca una placa por su número (alfanumérico).
     */
    Plate findByPlateNumber(String plateNumber);

    /**
     * Recupera todas las placas registradas.
     */
    List<Plate> findAll();
}