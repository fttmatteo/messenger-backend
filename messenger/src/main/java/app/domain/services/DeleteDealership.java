package app.domain.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import app.domain.ports.DealershipPort;

@Service
public class DeleteDealership {

    @Autowired
    private DealershipPort dealershipPort;

    public void deleteById(Long idDealership) throws Exception {
        dealershipPort.deleteById(idDealership);
    }
}