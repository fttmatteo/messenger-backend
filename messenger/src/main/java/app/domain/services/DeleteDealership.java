package app.domain.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import app.application.exceptions.BusinessException;
import app.domain.model.Dealership;
import app.domain.ports.DealershipPort;

@Service
public class DeleteDealership {

    @Autowired
    private DealershipPort dealershipPort;

    public void deleteById(Long id) throws Exception {
        Dealership existing = dealershipPort.findById(id);
        if (existing == null) {
            throw new BusinessException("El concesionario a eliminar no existe.");
        }
        var services = dealershipPort.findActiveServicesByDealershipId(id);
        if (services != null && !services.isEmpty()) {
            throw new BusinessException("No se puede eliminar el concesionario porque tiene servicios activos asociados.");
        }
        dealershipPort.deleteById(id);
    }

    public void deleteByName(String name) throws Exception {
        Dealership existing = dealershipPort.findByName(name);
        if (existing == null) {
            throw new BusinessException("El concesionario a eliminar no existe.");
        }
        var services = dealershipPort.findActiveServicesByDealershipName(name);
        if (services != null && !services.isEmpty()) {
            throw new BusinessException("No se puede eliminar el concesionario porque tiene servicios activos asociados.");
        }
        dealershipPort.deleteByName(name);
    }
}