package app.adapter.in.validators;

import app.application.exceptions.InputsException;

/**
 * Clase abstracta base para validadores.
 * 
 * Proporciona métodos utilitarios para validación de tipos básicos.
 */
public abstract class SimpleValidator {

    public String stringValidator(String element, String value) throws InputsException {
        if (value == null || value.trim().isEmpty()) {
            throw new InputsException(element + " no puede tener un valor vacío o nulo");
        }
        return value;
    }

    public int integerValidator(String element, String value) throws InputsException {
        stringValidator(element, value);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new InputsException(element + " debe ser un valor numérico entero");
        }
    }

    public long longValidator(String element, String value) throws InputsException {
        stringValidator(element, value);
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new InputsException(element + " debe ser un valor numérico");
        }
    }

    public double doubleValidator(String element, String value) throws InputsException {
        stringValidator(element, value);
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new InputsException(element + " debe ser un valor numérico");
        }
    }
}