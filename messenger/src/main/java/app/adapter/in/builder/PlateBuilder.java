package app.adapter.in.builder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import app.adapter.in.validators.PlateValidator;
import app.domain.model.Plate;
import app.domain.model.enums.PlateType;

@Component
public class PlateBuilder {

    @Autowired
    private PlateValidator validator;

    public Plate build(String plateNumber) throws Exception {
        String validPlate = validator.plateNumberValidator(plateNumber);

        Plate plate = new Plate();
        plate.setPlateNumber(validPlate);
        plate.setPlateType(identifyType(validPlate));

        return plate;
    }

    private PlateType identifyType(String plate) {
        if (plate.matches("^[A-Z]{3}\\s\\d{3}$")) {
            return PlateType.CAR;
        } else if (plate.matches("^[A-Z]{3}\\s\\d{2}[A-Z]$")) {
            return PlateType.MOTORCYCLE;
        } else if (plate.matches("^\\d{3}\\s[A-Z]{3}$")) {
            return PlateType.MOTORCAR;
        }
        throw new IllegalArgumentException("Tipo de placa no reconocido");
    }
}