package app.adapter.in.builder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import app.adapter.in.validators.PlateValidator;
import app.domain.model.Plate;

@Component
public class PlateBuilder {

    @Autowired
    private PlateValidator validator;

    public Plate build(String plateNumber, Long idDealership) throws Exception {
        validator.validateCreatePlate(plateNumber, idDealership);
        Plate plate = new Plate();
        plate.setPlateNumber(validator.plateNumberValidator(plateNumber));
        plate.setPlateType(validator.identifyPlateType(plateNumber));
        return plate;
    }
}