package app.domain.ports;

import app.domain.model.Dealership;
import java.util.List;

/**
 * Puerto (interfaz) para operaciones de persistencia de concesionarios.
 * 
 * Define las operaciones necesarias para gestionar concesionarios de vehículos,
 * incluyendo datos de ubicación y contacto.
 */
public interface DealershipPort {

    Dealership save(Dealership dealership);

    void deleteById(Long idDealership);

    Dealership findById(Long idDealership);

    Dealership findByName(String name);

    List<Dealership> findAll();
}