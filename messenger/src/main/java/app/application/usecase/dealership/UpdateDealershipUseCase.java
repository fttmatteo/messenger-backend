package app.application.usecase.dealership;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import app.domain.exception.BusinessException;
import app.domain.model.Dealership;
import app.domain.ports.DealershipPort;
import app.domain.util.LogSanitizer;

/**
 * Servicio para actualizar datos de concesionarios.
 */
@Service
public class UpdateDealershipUseCase {

    private static final Logger logger = LoggerFactory.getLogger(UpdateDealershipUseCase.class);

    @Autowired
    private DealershipPort dealershipPort;

    /**
     * Actualiza la información de un concesionario, validando unicidad de nombre.
     */
    public Dealership update(Long id, Dealership incomingData) throws Exception {
        Dealership existingDealership = dealershipPort.findById(id);
        if (existingDealership == null) {
            logger.warn("Intento de actualizar concesionario inexistente.");
            throw new BusinessException("El concesionario indicado no existe.");
        }

        if (!existingDealership.getName().equalsIgnoreCase(incomingData.getName())) {
            Dealership other = dealershipPort.findByName(incomingData.getName());
            if (other != null) {
                logger.warn("Intento de actualizar concesionario con nombre duplicado.");
                throw new BusinessException("Ya existe otro concesionario con ese nombre.");
            }
            existingDealership.setName(incomingData.getName());
        }

        if (incomingData.getWhatsappPin() != null && !incomingData.getWhatsappPin().isBlank() &&
                (existingDealership.getWhatsappPin() == null ||
                        !existingDealership.getWhatsappPin().equals(incomingData.getWhatsappPin()))) {
            Dealership withPin = dealershipPort.findByWhatsappPin(incomingData.getWhatsappPin());
            if (withPin != null && !withPin.getIdDealership().equals(existingDealership.getIdDealership())) {
                logger.warn("Intento de actualizar concesionario con PIN de WhatsApp duplicado: {}", LogSanitizer.maskPin(incomingData.getWhatsappPin()));
                throw new BusinessException("El PIN de WhatsApp ya está en uso.");
            }
        }

        existingDealership.setAddress(incomingData.getAddress());
        existingDealership.setPhone(incomingData.getPhone());
        existingDealership.setZone(incomingData.getZone());
        existingDealership.setWhatsappPin(incomingData.getWhatsappPin());

        Dealership updated = dealershipPort.save(existingDealership);
        logger.info("Concesionario actualizado exitosamente.");
        return updated;
    }
}
