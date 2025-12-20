package app.domain.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import app.application.exceptions.BusinessException;
import app.domain.model.Dealership;
import app.domain.ports.DealershipPort;
import app.domain.ports.ServiceDeliveryPort;

/**
 * Servicio de dominio para eliminar concesionarios.
 * 
 * Valida que el concesionario no tenga servicios activos asociados antes
 * de permitir su eliminación.
 */
@Service
public class DeleteDealership {

    private static final Logger logger = LoggerFactory.getLogger(DeleteDealership.class);

    @Autowired
    private DealershipPort dealershipPort;
    @Autowired
    private ServiceDeliveryPort serviceDeliveryPort;

    /**
     * Elimina un concesionario por su ID.
     * 
     * @param id ID del concesionario a eliminar.
     * @throws Exception Si el concesionario no existe o tiene servicios activos.
     */
    public void deleteById(Long id) throws Exception {
        logger.warn("Solicitud de eliminación de concesionario por ID: {}", id);
        Dealership existing = dealershipPort.findById(id);
        if (existing == null) {
            throw new BusinessException("El concesionario a eliminar no existe.");
        }
        var services = serviceDeliveryPort.findByDealershipId(id);
        if (services != null && !services.isEmpty()) {
            throw new BusinessException(
                    "No se puede eliminar el concesionario porque tiene servicios activos asociados.");
        }
        dealershipPort.deleteById(id);
        logger.info("Concesionario eliminado: ID {} ({})", id, existing.getName());
    }
}