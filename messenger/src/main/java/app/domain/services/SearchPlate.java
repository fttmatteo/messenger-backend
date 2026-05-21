package app.domain.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import app.domain.model.Plate;
import app.domain.ports.PlatePort;
import app.domain.util.LogSanitizer;

/**
 * Servicio para búsqueda de placas vehiculares.
 */
@Service
public class SearchPlate {

    private static final Logger logger = LoggerFactory.getLogger(SearchPlate.class);

    @Autowired
    private PlatePort platePort;

    public Plate findById(Long idPlate) {
        Plate plate = platePort.findById(idPlate);
        if (plate == null) {
            logger.warn("Placa no encontrada: ID {}", idPlate);
            throw new RuntimeException("El chasis no existe.");
        }
        return plate;
    }

    /**
     * Busca un chasis por su número alfanumérico.
     */
    public Plate findByPlateNumber(String plateNumber) {
        Plate plate = platePort.findByPlateNumber(plateNumber);
        if (plate == null) {
            logger.warn("Placa no encontrada: {}", LogSanitizer.maskPlate(plateNumber));
            throw new RuntimeException("El chasis no existe.");
        }
        return plate;
    }
}
