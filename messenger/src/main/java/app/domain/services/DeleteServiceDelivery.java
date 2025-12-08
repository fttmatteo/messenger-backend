package app.domain.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import app.application.exceptions.BusinessException;
import app.domain.model.ServiceDelivery;
import app.domain.model.enums.Status;
import app.domain.ports.ServiceDeliveryPort;

@Service
public class DeleteServiceDelivery {

    @Autowired
    private ServiceDeliveryPort serviceDeliveryPort;

    public void deleteById(Long id) throws Exception {
        ServiceDelivery service = serviceDeliveryPort.findById(id);
        if (service == null) {
            throw new BusinessException("El servicio de entrega que intenta eliminar no existe.");
        }

        var status = service.getCurrentStatus();
        if (status == Status.DELIVERED) {
            throw new BusinessException("El servicio de entrega que intenta eliminar ya está finalizado.");
        }

        serviceDeliveryPort.deleteById(id);
    }
}