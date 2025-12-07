package app.domain.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import app.application.exceptions.BusinessException;
import app.domain.model.Plate;
import app.domain.ports.PlatePort;

@Service
public class CreatePlate {

    @Autowired
    private PlatePort platePort;

    public void create(Plate plate) throws Exception {
        Plate existing = platePort.findByPlateNumber(plate.getPlateNumber());
        if (existing != null) {
            throw new BusinessException("La placa " + plate.getPlateNumber() + " ya está registrada.");
        }
        platePort.save(plate);
    }
}