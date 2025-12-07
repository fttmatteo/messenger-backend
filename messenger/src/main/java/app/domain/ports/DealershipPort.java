package app.domain.ports;

import app.domain.model.Dealership;

public interface DealershipPort {
    void save(Dealership dealership) throws Exception;

    void deleteById(Long idDealership) throws Exception;

    Dealership findByName(Dealership dealership) throws Exception;

    Dealership findById(Long idDealership) throws Exception;
}
