package app.domain.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import app.application.exceptions.BusinessException;
import app.domain.model.Dealership;
import app.domain.ports.DealershipPort;

/**
 * Servicio de dominio para actualizar información de concesionarios existentes.
 * 
 * Valida unicidad del nombre si se modifica y actualiza los datos del
 * concesionario.
 */
@Service
public class UpdateDealership {

    @Autowired
    private DealershipPort dealershipPort;

    /**
     * Actualiza la información de un concesionario existente.
     * 
     * @param id           ID del concesionario a actualizar.
     * @param incomingData Nuevos datos del concesionario.
     * @throws Exception Si el concesionario no existe o el nuevo nombre ya está en
     *                   uso.
     */
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