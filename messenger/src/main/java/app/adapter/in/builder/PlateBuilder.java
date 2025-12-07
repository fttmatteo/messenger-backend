package app.adapter.in.builder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import app.adapter.in.validators.PlateValidator;
import app.domain.model.Plate;

@Component
public class PlateBuilder {

    @Autowired
    private PlateValidator validator;

    public Plate build(String rawInput, Long idDealership) throws Exception {
        validator.validateCreatePlate(rawInput, idDealership);
        Plate plate = new Plate();
        String cleanPlate = validator.plateNumberValidator(rawInput);
        plate.setPlateNumber(cleanPlate);
        plate.setPlateType(validator.identifyPlateType(cleanPlate));
        return plate;
    }
}