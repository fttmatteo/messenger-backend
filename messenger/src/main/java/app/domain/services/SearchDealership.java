package app.domain.services;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import app.domain.model.Dealership;
import app.domain.ports.DealershipPort;

@Service
public class SearchDealership {

    @Autowired
    private DealershipPort dealershipPort;

    public List<Dealership> findAll() {
        return dealershipPort.findAll();
    }

    public Dealership findById(Long id) {
        Dealership dealership = dealershipPort.findById(id);
        if (dealership == null) {
            throw new RuntimeException("El concesionario con ID " + id + " no existe.");
        }
        return dealership;
    }

    public Dealership findByName(String name) {
        Dealership dealership = dealershipPort.findByName(name);
        if (dealership == null) {
            throw new RuntimeException("El concesionario con nombre " + name + " no existe.");
        }
        return dealership;
    }
}