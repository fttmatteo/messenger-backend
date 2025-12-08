package app.domain.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import app.application.exceptions.BusinessException;
import app.domain.model.ServiceDelivery;
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

        // 2. Reglas de negocio opcionales (Integridad)
        // Podríamos restringir la eliminación si el servicio ya está finalizado,
        // pero dado que esta función suele ser administrativa para corrección de
        // errores,
        // permitimos la eliminación directa.

        serviceDeliveryPort.deleteById(id);
    }
}