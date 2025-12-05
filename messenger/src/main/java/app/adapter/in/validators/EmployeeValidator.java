package app.adapter.in.validators;

import java.util.regex.Pattern;
import org.springframework.stereotype.Component;
import app.application.exceptions.InputsException;
import app.domain.model.enums.Zone;

@Component
public class EmployeeValidator extends SimpleValidator {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z0-9]+$");

    public String fullNameValidator(String value) throws InputsException {
        return stringValidator("nombre completo", value);
    }

    public long documentValidator(String value) throws InputsException {
        long doc = longValidator("número de cédula", value);
        if (String.valueOf(Math.abs(doc)).length() > 10) {
            throw new InputsException("la cédula no puede exceder 10 dígitos");
        }
        return doc;
    }

    public String phoneValidator(String value) throws InputsException {
        stringValidator("número de teléfono", value);
        if (!value.matches("\\d{1,10}")) {
            throw new InputsException("el número de teléfono debe contener entre 1 y 10 dígitos");
        }
        return value;
    }

    public String userNameValidator(String value) throws InputsException {
        stringValidator("nombre de usuario", value);
        if (value.length() > 15) {
            throw new InputsException("el nombre de usuario no puede exceder 15 caracteres");
        }
        if (!USERNAME_PATTERN.matcher(value).matches()) {
            throw new InputsException("el nombre de usuario solo debe contener letras y números");
        }
        return value;
    }

    public String passwordValidator(String value) throws InputsException {
        stringValidator("contraseña", value);
        if (value.length() < 8) {
            throw new InputsException("la contraseña debe contener al menos 8 caracteres");
        }
        if (!value.matches(".*[A-Z].*")) {
            throw new InputsException("la contraseña debe contener al menos una letra mayúscula");
        }
        if (!value.matches(".*[0-9].*")) {
            throw new InputsException("la contraseña debe contener al menos un número");
        }
        if (!value.matches(".*[^A-Za-z0-9].*")) {
            throw new InputsException("la contraseña debe contener al menos un carácter especial");
        }
        return value;
    }

    public Zone zoneValidator(String value) throws InputsException {
        stringValidator("zona", value);
        String v = value.trim().toLowerCase();

        for (Zone z : Zone.values()) {
            if (z.name().equalsIgnoreCase(v)) {
                return z;
            }
        }

        switch (v) {
            case "norte":
                return Zone.NORTH;
            case "sur":
                return Zone.SOUTH;
            case "centro":
                return Zone.CENTER;
            default:
                throw new InputsException("la zona debe ser norte, sur o centro");
        }
    }
}