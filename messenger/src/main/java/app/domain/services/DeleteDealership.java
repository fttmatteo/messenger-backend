package app.domain.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import app.domain.exception.BusinessException;
import app.domain.model.Dealership;
import app.domain.ports.DealershipPort;

/**
 * Servicio para eliminar concesionarios.
 */
@Service
public class DeleteDealership {

    private static final Logger logger = LoggerFactory.getLogger(DeleteDealership.class);

    @Autowired
    private DealershipPort dealershipPort;

    /**
     * Elimina un concesionario por su ID, verificando previamente su existencia.
     */
    public void deleteById(Long id) throws Exception {
        Dealership existing = dealershipPort.findById(id);
        if (existing == null) {
            logger.warn("Intento de eliminar concesionario inexistente.");
            throw new BusinessException("El concesionario indicado no existe.");
        }
        dealershipPort.deleteById(id);
    }
}
