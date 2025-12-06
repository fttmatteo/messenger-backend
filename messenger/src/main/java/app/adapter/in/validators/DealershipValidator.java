package app.adapter.in.validators;

import app.application.exceptions.InputsException;

public class DealershipValidator extends SimpleValidator {
    public String nameValidator(String value) throws InputsException {
        return stringValidator("nombre", value);
    }

    public String addressValidator(String value) throws InputsException {
        return stringValidator("direccion", value);
    }

    public String phoneValidator(String value) throws InputsException {
        return stringValidator("telefono", value);
    }

    public String zoneValidator(String value) throws InputsException {
        return stringValidator("zona", value);
    }
}
