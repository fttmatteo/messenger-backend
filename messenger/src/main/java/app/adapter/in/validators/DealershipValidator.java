package app.adapter.in.validators;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import app.application.exceptions.BusinessException;
import app.domain.model.Dealership;
import app.domain.model.ServiceDelivery;
import app.domain.ports.DealershipPort;
import app.domain.ports.ServiceDeliveryPort;

@Component
public class DealershipValidator extends SimpleValidator {

    @Autowired
    private DealershipPort dealershipPort;

    @Autowired
    private ServiceDeliveryPort serviceDeliveryPort;

    public void validateCreation(Dealership dealership) throws Exception {
        if (dealershipPort.findByName(dealership.getName()) != null) {
            throw new BusinessException("Ya existe un concesionario con el nombre: " + dealership.getName());
        }
    }

    public void validateUpdate(Dealership newDealership, Dealership existingDealership) throws Exception {
        if (!existingDealership.getName().equalsIgnoreCase(newDealership.getName())) {
             if (dealershipPort.findByName(newDealership.getName()) != null) {
                 throw new BusinessException("Ya existe otro concesionario con el nombre: " + newDealership.getName());
             }
        }
    }

    public void validateDelete(Long dealershipId) throws BusinessException {
        // Validamos que no tenga servicios asociados
        // Nota: Idealmente ServiceDeliveryPort tendría findByDealershipId, usamos findAll como fallback seguro.
        List<ServiceDelivery> allServices = serviceDeliveryPort.findAll();
        
        boolean hasDependencies = allServices.stream()
            .anyMatch(service -> service.getDealership() != null && 
                                 service.getDealership().getIdDealership().equals(dealershipId));

        if (hasDependencies) {
            throw new BusinessException("NO SE PUEDE ELIMINAR: El concesionario tiene servicios de entrega asociados.");
        }
    }
}