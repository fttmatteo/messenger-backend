package app.adapter.in.validators;

import java.util.regex.Pattern;
import org.springframework.stereotype.Component;
import app.application.exceptions.InputsException;

@Component
public class EmployeeValidator extends SimpleValidator {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z0-9]+$");

    public String fullNameValidator(String value) throws InputsException {
        return stringValidator("nombre completo", value);
    }

    public long documentValidator(String value) throws InputsException {
        long doc = longValidator("número de cédula", value);
        if (String.valueOf(Math.abs(doc)).length() > 10) {
            throw new InputsException("La cédula no puede exceder 10 dígitos");
        }
        return doc;
    }

    public String phoneValidator(String value) throws InputsException {
        stringValidator("número de teléfono", value);
        if (!value.matches("\\d{10}")) {
            throw new InputsException("El número de teléfono debe contener 10 dígitos");
        }
        return value;
    }

    public String userNameValidator(String value) throws InputsException {
        stringValidator("nombre de usuario", value);
        if (value.length() > 15) {
            throw new InputsException("El nombre de usuario no puede exceder 15 caracteres");
        }
        if (!USERNAME_PATTERN.matcher(value).matches()) {
            throw new InputsException("El nombre de usuario solo debe contener letras y números");
        }
        return value;
    }

    public String passwordValidator(String value) throws InputsException {
        stringValidator("contraseña", value);
        if (value.length() < 8) {
            throw new InputsException("La contraseña debe contener al menos 8 caracteres");
        }
        if (!value.matches(".*[A-Z].*")) {
            throw new InputsException("La contraseña debe contener al menos una letra mayúscula");
        }
        if (!value.matches(".*[0-9].*")) {
            throw new InputsException("La contraseña debe contener al menos un número");
        }
        if (!value.matches(".*[^A-Za-z0-9].*")) {
            throw new InputsException("La contraseña debe contener al menos un carácter especial");
        }
        return value;
    }
}