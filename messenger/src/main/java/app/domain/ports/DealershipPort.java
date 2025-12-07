package app.domain.ports;

import app.domain.model.Dealership;
import java.util.List;

public interface DealershipPort {
    Dealership save(Dealership dealership);

    Dealership update(Dealership dealership);

    void deleteByName(String dealershipName);

    void deleteById(Long idDealership);

    Dealership findById(Long idDealership);

    Dealership findByDealershipName(String dealershipName);

    List<Dealership> findAll();
}