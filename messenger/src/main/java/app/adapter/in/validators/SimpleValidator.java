package app.adapter.in.validators;

import app.application.exceptions.InputsException;
import org.springframework.web.multipart.MultipartFile;

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

    public MultipartFile fileValidator(String element, MultipartFile file) throws InputsException {
        if (file == null || file.isEmpty()) {
            throw new InputsException(element + " es obligatorio y no puede estar vacío");
        }
        return file;
    }

    public void notNullValidator(String element, Object value) throws InputsException {
        if (value == null) {
            throw new InputsException(element + " es obligatorio");
        }
    }

}