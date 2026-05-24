package app.adapter.in.rest.delivery;
import app.adapter.in.rest.common.SimpleValidator;

import org.springframework.stereotype.Component;
import app.domain.exception.InputsException;
import app.domain.model.enums.Status;

/**
 * Validador de inputs para servicios de entrega.
 */
@Component
public class ServiceDeliveryValidator extends SimpleValidator {

    public Long idValidator(String value) throws InputsException {
        return longValidator("ID", value);
    }

    public Long documentValidator(String value) throws InputsException {
        return longValidator("documento de usuario", value);
    }

    public Status statusValidator(String value) throws InputsException {
        stringValidator("estado", value);
        try {
            return Status.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InputsException("Estado inválido.");
        }
    }

    public String observationValidator(String value) throws InputsException {
        if (value != null && !value.trim().isEmpty()) {
            return value.trim();
        }
        return value;
    }

    public String plateNumberValidator(String value) throws InputsException {
        if (value == null || value.trim().isEmpty()) {
            throw new InputsException("El número de chasis no puede estar vacío.");
        }

        String cleaned = value.toUpperCase().trim();

        cleaned = cleaned.replaceAll("[^A-Z0-9]", "");

        if (cleaned.length() < 10 || cleaned.length() > 20) {
            throw new InputsException("El número de chasis debe tener entre 10 y 20 caracteres. Recibido: " + cleaned.length());
        }

        if (!cleaned.matches("^[A-Z0-9]+$")) {
            throw new InputsException("Formato de chasis inválido. Solo se permiten letras y números.");
        }
        return cleaned;
    }
}
