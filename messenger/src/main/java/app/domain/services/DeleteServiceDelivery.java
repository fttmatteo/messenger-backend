package app.domain.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import app.application.exceptions.BusinessException;
import app.domain.model.ServiceDelivery;
import app.domain.model.enums.Status;
import app.domain.ports.ServiceDeliveryPort;

/**
 * Servicio de dominio para eliminar servicios de entrega.
 * 
 * Valida que el servicio no esté en estado DELIVERED antes de permitir
 * su eliminación.
 */
@Service
public class DeleteServiceDelivery {

    private static final Logger logger = LoggerFactory.getLogger(DeleteServiceDelivery.class);

    @Autowired
    private ServiceDeliveryPort serviceDeliveryPort;

    /**
     * Elimina un servicio de entrega por su ID.
     * 
     * @param id ID del servicio a eliminar.
     * @throws Exception Si el servicio no existe o ya está en estado DELIVERED.
     */
    public void deleteById(Long id) throws Exception {
        logger.warn("Solicitud de eliminación de servicio de entrega ID: {}", id);
        ServiceDelivery service = serviceDeliveryPort.findById(id);
        if (service == null) {
            throw new BusinessException("El servicio de entrega que intenta eliminar no existe.");
        }

        var status = service.getCurrentStatus();
        if (status == Status.DELIVERED) {
            throw new BusinessException("El servicio de entrega que intenta eliminar ya está finalizado.");
        }

        serviceDeliveryPort.deleteById(id);
        logger.info("Servicio de entrega eliminado: ID {} (placa: {})", id, service.getPlate().getPlateNumber());
    }
}