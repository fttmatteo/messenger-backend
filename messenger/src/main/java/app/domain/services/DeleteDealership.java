package app.domain.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import app.application.exceptions.BusinessException;
import app.domain.model.Dealership;
import app.domain.ports.DealershipPort;

/**
 * Servicio de dominio para eliminar concesionarios.
 */
@Service
public class DeleteDealership {

    private static final Logger logger = LoggerFactory.getLogger(DeleteDealership.class);

    @Autowired
    private DealershipPort dealershipPort;

    public void deleteById(Long id) throws Exception {
        logger.warn("Solicitud de eliminación de concesionario por ID: {}", id);
        Dealership existing = dealershipPort.findById(id);
        if (existing == null) {
            throw new BusinessException("El concesionario a eliminar no existe.");
        }
        dealershipPort.deleteById(id);
        logger.info("Concesionario eliminado: ID {} ({})", id, existing.getName());
    }
}