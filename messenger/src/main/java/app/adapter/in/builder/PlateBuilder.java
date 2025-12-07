package app.adapter.in.builder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import app.adapter.in.validators.PlateValidator;
import app.domain.model.Plate;

@Component
public class PlateBuilder {

    @Autowired
    private PlateValidator validator;

    public Plate build(String plateNumber) throws Exception {
        String validPlate = validator.plateNumberValidator(plateNumber);
        Plate plate = new Plate();
        plate.setPlateNumber(validPlate);
        plate.setPlateType(validator.identifyPlateType(validPlate));
        return plate;
    }
}