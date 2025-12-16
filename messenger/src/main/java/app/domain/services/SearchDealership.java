package app.domain.services;

import java.util.List;
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

    @Autowired
    private DealershipPort dealershipPort;

    /**
     * Obtiene todos los concesionarios registrados.
     * 
     * @return Lista completa de concesionarios.
     */
    public List<Dealership> findAll() {
        return dealershipPort.findAll();
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