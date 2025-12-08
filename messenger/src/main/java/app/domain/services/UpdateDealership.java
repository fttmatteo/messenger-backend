package app.domain.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import app.application.exceptions.BusinessException;
import app.domain.model.Dealership;
import app.domain.ports.DealershipPort;

@Service
public class UpdateDealership {

    @Autowired
    private DealershipPort dealershipPort;

    public void update(Long id, Dealership incomingData) throws Exception {
        Dealership existingDealership = dealershipPort.findById(id);
        if (existingDealership == null) {
            throw new BusinessException("El concesionario con ID " + id + " no existe.");
        }

        if (!existingDealership.getName().equalsIgnoreCase(incomingData.getName())) {
            Dealership other = dealershipPort.findByName(incomingData.getName());
            if (other != null) {
                throw new BusinessException("Ya existe otro concesionario con el nombre " + incomingData.getName());
            }
            existingDealership.setName(incomingData.getName());
        }

        existingDealership.setAddress(incomingData.getAddress());
        existingDealership.setPhone(incomingData.getPhone());
        existingDealership.setZone(incomingData.getZone());

        dealershipPort.save(existingDealership);
    }
}