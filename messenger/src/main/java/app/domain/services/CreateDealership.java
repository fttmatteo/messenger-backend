package app.domain.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import app.domain.model.Dealership;
import app.domain.ports.DealershipPort;
import app.application.exceptions.BusinessException;

@Service
public class CreateDealership {

    @Autowired
    private DealershipPort dealershipPort;

    public void create(Dealership dealership) throws Exception {
        Dealership existing = dealershipPort.findByName(dealership);
        if (existing != null) {
            throw new BusinessException("Ya existe un concesionario registrado con ese nombre");
        }
        dealershipPort.save(dealership);
    }
}
