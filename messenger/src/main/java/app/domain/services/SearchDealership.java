package app.domain.services;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import app.application.exceptions.ResourceNotFoundException;
import app.domain.model.Dealership;
import app.domain.ports.DealershipPort;

/**
 * Servicio de dominio para búsqueda y recuperación de concesionarios.
 * 
 * Proporciona búsqueda por ID, nombre y listado completo.
 */
@Service
public class SearchDealership {

    private static final Logger logger = LoggerFactory.getLogger(SearchDealership.class);

    @Autowired
    private DealershipPort dealershipPort;

    /**
     * Obtiene todos los concesionarios registrados.
     * 
     * @return Lista completa de concesionarios.
     */
    public List<Dealership> findAll() {
        logger.debug("Buscando todos los concesionarios");
        List<Dealership> dealerships = dealershipPort.findAll();
        logger.debug("Concesionarios encontrados: {}", dealerships.size());
        return dealerships;
    }

    /**
     * Busca un concesionario por su ID.
     * 
     * @param id ID del concesionario.
     * @return Concesionario encontrado.
     * @throws app.application.exceptions.ResourceNotFoundException Si el
     *                                                              concesionario no
     *                                                              existe.
     */
    public Dealership findById(Long id) {
        Dealership dealership = dealershipPort.findById(id);
        if (dealership == null) {
            throw new ResourceNotFoundException(
                    "El concesionario con ID " + id + " no existe.");
        }
        return dealership;
    }

    /**
     * Busca un concesionario por su nombre.
     * 
     * @param name Nombre del concesionario.
     * @return Concesionario encontrado.
     * @throws RuntimeException Si el concesionario no existe.
     */
    public Dealership findByName(String name) {
        Dealership dealership = dealershipPort.findByName(name);
        if (dealership == null) {
            throw new RuntimeException("El concesionario con nombre " + name + " no existe.");
        }
        return dealership;
    }
}