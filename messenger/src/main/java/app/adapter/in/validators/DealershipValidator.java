package app.adapter.in.validators;

import org.springframework.stereotype.Component;
import app.application.exceptions.InputsException;

/**
 * Validador para concesionarios.
 * 
 * Aplica reglas de validación para datos de concesionarios.
 */
@Component
public class DealershipValidator extends SimpleValidator {

    public String nameValidator(String value) throws InputsException {
        return stringValidator("nombre del concesionario", value);
    }

    public String addressValidator(String value) throws InputsException {
        return stringValidator("dirección", value);
    }

    public String phoneValidator(String value) throws InputsException {
        stringValidator("teléfono", value);
        if (!value.matches("\\d{10}")) {
            throw new InputsException("El teléfono del concesionario debe tener 10 dígitos.");
        }
        return value;
    }

    public String zoneValidator(String value) throws InputsException {
        stringValidator("zona", value);
        if (!value.equalsIgnoreCase("centro") && !value.equalsIgnoreCase("sur") && !value.equalsIgnoreCase("norte")) {
            throw new InputsException("La zona debe ser centro, sur o norte");
        }
        return value;
    }
}