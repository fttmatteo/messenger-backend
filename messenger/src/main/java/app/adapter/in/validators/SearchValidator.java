package app.adapter.in.validators;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import app.application.exceptions.BusinessException;
import app.domain.ports.EmployeePort;
import app.domain.ports.ServiceDeliveryPort;

@Component
public class SearchValidator {

    @Autowired
    private EmployeePort employeePort;
    
    @Autowired
    private ServiceDeliveryPort serviceDeliveryPort;

    public void validateMessengerExists(Long messengerDocument) throws Exception {
        if (employeePort.findByDocument(messengerDocument) == null) {
            throw new BusinessException("No se pueden buscar servicios: El mensajero con documento " + messengerDocument + " no existe.");
        }
    }
    
    public void validateServiceExists(Long serviceId) throws Exception {
        if (serviceDeliveryPort.findById(serviceId) == null) {
            throw new BusinessException("El servicio de entrega con ID " + serviceId + " no existe.");
        }
    }
    
    // Podrías agregar validación de placa si tienes un PlatePort separado, 
    // pero como la placa es parte del servicio, a veces es opcional.
}