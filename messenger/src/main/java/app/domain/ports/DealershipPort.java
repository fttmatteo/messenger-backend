package app.domain.ports;

import app.domain.model.Dealership;

public interface DealershipPort {
    void save(Dealership dealership) throws Exception;
    void update(Dealership dealership) throws Exception;
    void deleteById(Long id) throws Exception;
    Dealership findById(Long id) throws Exception;
}
