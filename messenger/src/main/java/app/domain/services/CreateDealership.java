package app.domain.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import app.domain.model.Dealership;
import app.domain.ports.DealershipPort;

@Service
public class CreateDealership {

    @Autowired
    private DealershipPort dealershipPort;

    public void create(Dealership dealership) throws Exception {
        dealershipPort.save(dealership);
    }
}
