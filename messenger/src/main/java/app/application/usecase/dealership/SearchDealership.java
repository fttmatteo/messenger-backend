package app.application.usecase.dealership;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import app.domain.exception.ResourceNotFoundException;
import app.domain.model.Dealership;
import app.domain.ports.DealershipPort;

/**
 * Servicio para búsqueda de concesionarios.
 */
@Service
public class SearchDealership {

    private static final Logger logger = LoggerFactory.getLogger(SearchDealership.class);

    @Autowired
    private DealershipPort dealershipPort;

    /**
     * Obtiene todos los concesionarios registrados.
     */
    public List<Dealership> findAll() {
        List<Dealership> dealerships = dealershipPort.findAll();
        return dealerships;
    }

    /**
     * Busca un concesionario por su ID.
     */
    public Dealership findById(Long id) {
        Dealership dealership = dealershipPort.findById(id);
        if (dealership == null) {
            logger.warn("Concesionario no encontrado por ID.");
            throw new ResourceNotFoundException(
                    "El concesionario no existe.");
        }
        return dealership;
    }

    /**
     * Busca un concesionario por su nombre exacto.
     */
    public Dealership findByName(String name) {
        Dealership dealership = dealershipPort.findByName(name);
        if (dealership == null) {
            logger.warn("Concesionario no encontrado por nombre.");
            throw new RuntimeException("El concesionario no existe.");
        }
        return dealership;
    }

    /**
     * Busca un concesionario por su UUID público.
     */
    public Dealership findByUuid(String uuid) {
        Dealership dealership = dealershipPort.findByUuid(uuid);
        if (dealership == null) {
            logger.warn("Concesionario no encontrado por UUID.");
            throw new ResourceNotFoundException("El concesionario no existe.");
        }
        return dealership;
    }
}
