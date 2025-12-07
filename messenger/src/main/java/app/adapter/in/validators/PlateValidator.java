package app.adapter.in.validators;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import app.application.exceptions.InputsException;
import app.domain.model.enums.PlateType;

@Component
public class PlateValidator extends SimpleValidator {

    public String plateNumberValidator(String value) throws InputsException {
        String plate = stringValidator("número de placa", value).toUpperCase();
        String regex = "^([A-Z]{3}\\s\\d{2}[A-Z]|[A-Z]{3}\\s\\d{3}|\\d{3}\\s[A-Z]{3})$";

        if (!plate.matches(regex)) {
            throw new InputsException(
                    "El formato de la placa no es válido. Formatos aceptados: 'ABC 123', 'ABC 12A', '123 ABC'");
        }
        return plate;
    }

    public PlateType identifyPlateType(String plate) {
        if (plate.matches("^[A-Z]{3}\\s\\d{3}$")) {
            return PlateType.CAR;
        } else if (plate.matches("^[A-Z]{3}\\s\\d{2}[A-Z]$")) {
            return PlateType.MOTORCYCLE;
        } else if (plate.matches("^\\d{3}\\s[A-Z]{3}$")) {
            return PlateType.MOTORCAR;
        }
        return null;
    }

    public void validateOCRInput(MultipartFile image, Long idDealership) throws InputsException {
        notNullValidator("El concesionario", idDealership);
        fileValidator("La foto de la placa", image);
    }

    public void validateCreatePlate(String plateNumber, Long idDealership) throws InputsException {
        plateNumberValidator(plateNumber);
        notNullValidator("El id del concesionario", idDealership);
    }
}