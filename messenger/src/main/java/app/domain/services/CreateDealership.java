package app.domain.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import app.application.exceptions.BusinessException;
import app.domain.model.Dealership;
import app.domain.ports.DealershipPort;

@Service
public class CreateDealership {

    @Autowired
    private DealershipPort dealershipPort;

    public Dealership create(Dealership dealership) throws Exception {
        Dealership existing = dealershipPort.findByDealershipName(dealership.getName());
        if (existing != null) {
            throw new BusinessException("Ya existe un concesionario con el nombre: " + dealership.getName());
        }
        return dealershipPort.save(dealership);
    }
}