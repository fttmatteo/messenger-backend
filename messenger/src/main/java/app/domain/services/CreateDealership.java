package app.domain.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import app.domain.exception.BusinessException;
import app.domain.model.Dealership;
import app.domain.ports.DealershipPort;

/**
 * Servicio para crear nuevos concesionarios.
 */
@Service
public class CreateDealership {

    @Autowired
    private DealershipPort dealershipPort;

    public Dealership create(Dealership dealership) throws Exception {
        Dealership existing = dealershipPort.findByName(dealership.getName());
        if (existing != null) {
            throw new BusinessException("Ya existe un concesionario con el nombre " + dealership.getName());
        }

        if (dealership.getWhatsappPin() != null && !dealership.getWhatsappPin().isBlank()) {
            Dealership withPin = dealershipPort.findByWhatsappPin(dealership.getWhatsappPin());
            if (withPin != null) {
                throw new BusinessException("El PIN de WhatsApp " + dealership.getWhatsappPin() + " ya está en uso.");
            }
        }

        Dealership saved = dealershipPort.save(dealership);
        return saved;
    }
}