package app.domain.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import app.application.exceptions.BusinessException;
import app.domain.ports.PlatePort;

@Service
public class DeletePlate {

    @Autowired
    private PlatePort platePort;

    public void deleteById(Long idPlate) throws Exception {
        if (platePort.findById(idPlate) == null) {
            throw new BusinessException("No se encontró una placa con el ID proporcionado.");
        }
        platePort.deleteById(idPlate);
    }
}