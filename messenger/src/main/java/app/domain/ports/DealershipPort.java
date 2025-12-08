// Archivo: app/domain/ports/DealershipPort.java
package app.domain.ports;

import app.domain.model.Dealership;
import java.util.List;

public interface DealershipPort {
    Dealership save(Dealership dealership);

    void deleteById(Long idDealership);

    void deleteByName(String dealershipName);

    Dealership findById(Long idDealership);

    Dealership findByName(String name);

    List<Dealership> findAll();
}