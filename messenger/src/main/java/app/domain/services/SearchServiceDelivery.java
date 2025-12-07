package app.domain.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import app.adapter.in.validators.SearchValidator;
import app.domain.model.ServiceDelivery;
import app.domain.model.enums.Status;
import app.domain.ports.ServiceDeliveryPort;

@Service
public class SearchServiceDelivery {

    @Autowired
    private ServiceDeliveryPort serviceDeliveryPort;
    @Autowired
    private SearchValidator validator;

    public List<ServiceDelivery> findAll() {
        return serviceDeliveryPort.findAll();
    }

    public ServiceDelivery findById(Long id) throws Exception {
        // Opcional: Validar que exista antes de retornar, o dejar que retorne null.
        // Si quieres ser estricto como en veterinaria:
        validator.validateServiceExists(id);
        return serviceDeliveryPort.findById(id);
    }

    public List<ServiceDelivery> findByStatus(Status status) {
        return serviceDeliveryPort.findByStatus(status);
    }

    public List<ServiceDelivery> findByMessenger(Long messengerDocument) throws Exception {
        // Validamos que el mensajero exista antes de hacer la búsqueda
        validator.validateMessengerExists(messengerDocument);
        return serviceDeliveryPort.findByMessengerDocument(messengerDocument);
    }
    
    public List<ServiceDelivery> findByPlate(String plateNumber) {
        // Búsqueda directa (puedes agregar validador si tuvieras PlatePort)
        return serviceDeliveryPort.findByPlateNumber(plateNumber);
    }
}