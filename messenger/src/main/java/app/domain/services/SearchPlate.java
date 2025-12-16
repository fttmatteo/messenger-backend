package app.domain.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import app.domain.model.Plate;
import app.domain.ports.PlatePort;

/**
 * Servicio de dominio para búsqueda de placas vehiculares.
 * 
 * Proporciona búsqueda por ID y número de placa.
 */
@Service
public class SearchPlate {

    private static final Logger logger = LoggerFactory.getLogger(SearchPlate.class);

    @Autowired
    private PlatePort platePort;

    /**
     * Busca una placa por su ID.
     * 
     * @param idPlate ID de la placa.
     * @return Placa encontrada.
     * @throws RuntimeException Si la placa no existe.
     */
    public Plate findById(Long idPlate) {
        Plate plate = platePort.findById(idPlate);
        if (plate == null) {
            throw new RuntimeException("El placa con ID " + idPlate + " no existe.");
        }
        return plate;
    }

    /**
     * Busca una placa por su número.
     * 
     * @param plateNumber Número de placa.
     * @return Placa encontrada.
     * @throws RuntimeException Si la placa no existe.
     */
    public Plate findByPlateNumber(String plateNumber) {
        logger.debug("Buscando placa: {}", plateNumber);
        Plate plate = platePort.findByPlateNumber(plateNumber);
        if (plate == null) {
            throw new RuntimeException("El placa con número " + plateNumber + " no existe.");
        }
        return plate;
    }
}
