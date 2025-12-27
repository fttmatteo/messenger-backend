package app.domain.services;

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

    @Autowired
    private DealershipPort dealershipPort;

    /**
     * Elimina un concesionario por su ID, verificando previamente su existencia.
     */
    public void deleteById(Long id) throws Exception {
        Dealership existing = dealershipPort.findById(id);
        if (existing == null) {
            throw new BusinessException("El concesionario a eliminar no existe.");
        }
        dealershipPort.deleteById(id);
    }
}