package app.domain.ports;

import app.domain.model.Plate;
import java.util.List;

/**
 * Puerto (interfaz) para operaciones de persistencia de placas vehiculares.
 * 
 * Define las operaciones para almacenar y consultar placas detectadas por OCR.
 */
public interface PlatePort {
    /**
     * Guarda o actualiza una placa en la base de datos.
     * 
     * @param plate Placa a guardar.
     */
    void save(Plate plate);

    /**
     * Busca una placa por su ID.
     * 
     * @param idPlate ID de la placa.
     * @return Placa encontrada o null si no existe.
     */
    Plate findById(Long idPlate);

    /**
     * Busca una placa por su número.
     * 
     * @param plateNumber Número de placa (ej: "ABC123").
     * @return Placa encontrada o null si no existe.
     */
    Plate findByPlateNumber(String plateNumber);

    /**
     * Obtiene todas las placas registradas.
     * 
     * @return Lista de todas las placas.
     */
    List<Plate> findAll();
}