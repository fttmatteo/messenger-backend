package app.adapter.in.validators;

import org.springframework.stereotype.Component;
import app.application.exceptions.InputsException;

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

}