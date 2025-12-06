package app.adapter.in.validators;

import org.springframework.stereotype.Component;
import app.application.exceptions.InputsException;

@Component
public class DealershipValidator extends SimpleValidator {
    public String nameValidator(String value) throws InputsException {
        return stringValidator("nombre", value);
    }

    public String addressValidator(String value) throws InputsException {
        return stringValidator("direccion", value);
    }

    public String phoneValidator(String value) throws InputsException {
        stringValidator("número de teléfono", value);
        if (!value.matches("\\d{10}")) {
            throw new InputsException("el número de teléfono debe contener 10 dígitos");
        }
        return value;
    }

    public String zoneValidator(String value) throws InputsException {
        return stringValidator("zona", value);
    }
}
