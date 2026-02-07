package app.domain.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import app.domain.exception.BusinessException;
import app.domain.model.Dealership;
import app.domain.ports.DealershipPort;

/**
 * Servicio para actualizar datos de concesionarios.
 */
@Service
public class UpdateDealership {

    @Autowired
    private DealershipPort dealershipPort;

    /**
     * Actualiza la información de un concesionario, validando unicidad de nombre.
     */
    public Dealership update(Long id, Dealership incomingData) throws Exception {
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

        if (incomingData.getWhatsappPin() != null && !incomingData.getWhatsappPin().isBlank() &&
                (existingDealership.getWhatsappPin() == null ||
                        !existingDealership.getWhatsappPin().equals(incomingData.getWhatsappPin()))) {
            Dealership withPin = dealershipPort.findByWhatsappPin(incomingData.getWhatsappPin());
            if (withPin != null && !withPin.getIdDealership().equals(existingDealership.getIdDealership())) {
                throw new BusinessException("El PIN de WhatsApp " + incomingData.getWhatsappPin() + " ya está en uso.");
            }
        }

        existingDealership.setAddress(incomingData.getAddress());
        existingDealership.setPhone(incomingData.getPhone());
        existingDealership.setZone(incomingData.getZone());
        existingDealership.setWhatsappPin(incomingData.getWhatsappPin());

        Dealership updated = dealershipPort.save(existingDealership);
        return updated;
    }
}