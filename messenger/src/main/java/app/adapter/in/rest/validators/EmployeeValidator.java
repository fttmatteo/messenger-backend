package app.adapter.in.rest.validators;

import app.domain.exception.InputsException;
import app.domain.model.enums.Role;

import org.springframework.stereotype.Component;

/**
 * Validador de inputs para empleados.
 */
@Component
public class EmployeeValidator extends SimpleValidator {

    public long documentValidator(String value) throws InputsException {
        long doc = longValidator("número de cédula", value);
        if (String.valueOf(Math.abs(doc)).length() > 10) {
            throw new InputsException("La cédula no puede exceder 10 dígitos");
        }
        return doc;
    }

    public String fullNameValidator(String fullName) throws InputsException {
        return stringValidator("nombre completo", fullName);
    }

    public String phoneValidator(String value) throws InputsException {
        stringValidator("número de teléfono", value);
        if (!value.matches("\\d{10}")) {
            throw new InputsException("El número de teléfono debe contener exactamente 10 dígitos");
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

    public Role roleValidator(String value) throws InputsException {
        stringValidator("rol", value);
        try {
            return Role.valueOf(value.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new InputsException("Cargo inválido.");
        }
    }
}
