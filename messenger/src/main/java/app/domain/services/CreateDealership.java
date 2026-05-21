package app.domain.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import app.domain.exception.BusinessException;
import app.domain.model.Dealership;
import app.domain.ports.DealershipPort;
import app.domain.util.LogSanitizer;

/**
 * Servicio para crear nuevos concesionarios.
 */
@Service
public class CreateDealership {

    private static final Logger logger = LoggerFactory.getLogger(CreateDealership.class);

    @Autowired
    private DealershipPort dealershipPort;

    public Dealership create(Dealership dealership) throws Exception {
        Dealership existing = dealershipPort.findByName(dealership.getName());
        if (existing != null) {
            logger.warn("Intento de crear concesionario con nombre duplicado.");
            throw new BusinessException("Ya existe un concesionario con ese nombre.");
        }

        if (dealership.getWhatsappPin() != null && !dealership.getWhatsappPin().isBlank()) {
            Dealership withPin = dealershipPort.findByWhatsappPin(dealership.getWhatsappPin());
            if (withPin != null) {
                logger.warn("Intento de crear concesionario con PIN de WhatsApp duplicado: {}", LogSanitizer.maskPin(dealership.getWhatsappPin()));
                throw new BusinessException("El PIN de WhatsApp ya está en uso.");
            }
        }

        Dealership saved = dealershipPort.save(dealership);
        logger.info("Concesionario creado exitosamente.");
        return saved;
    }
}