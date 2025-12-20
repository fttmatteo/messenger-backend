package app.domain.ports;

import app.domain.model.Dealership;
import java.util.List;

/**
 * Puerto de salida para persistencia de concesionarios.
 */
public interface DealershipPort {

    Dealership save(Dealership dealership);

    void deleteById(Long idDealership);

    Dealership findById(Long idDealership);

    Dealership findByName(String name);

    List<Dealership> findAll();
}