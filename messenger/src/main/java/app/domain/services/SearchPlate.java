package app.domain.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import app.domain.model.Plate;
import app.domain.ports.PlatePort;

@Service
public class SearchPlate {

    @Autowired
    private PlatePort platePort;

    public Plate findById(Long idPlate) {
        Plate plate = platePort.findById(idPlate);
        if (plate == null) {
            throw new RuntimeException("El placa con ID " + idPlate + " no existe.");
        }
        return plate;
    }

    public Plate findByPlateNumber(String plateNumber) {
        Plate plate = platePort.findByPlateNumber(plateNumber);
        if (plate == null) {
            throw new RuntimeException("El placa con número " + plateNumber + " no existe.");
        }
        return plate;
    }
}
