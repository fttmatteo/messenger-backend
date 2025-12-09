package app.adapter.in.validators;

import org.springframework.stereotype.Component;
import app.application.exceptions.InputsException;
import app.domain.model.enums.Status;

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
            throw new InputsException(
                    "Estado inválido. Valores permitidos: ASIGNADO, ENTREGADO, PENDIENTE, FALLIDO, DEVUELTO, CANCELADO, OBSERVADO");
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
            throw new InputsException("El número de placa no puede estar vacío.");
        }

        String cleaned = value.toUpperCase().trim();

        cleaned = cleaned.replaceAll("O", "0")
                .replaceAll("I", "1")
                .replaceAll("[^A-Z0-9]", "");

        if (cleaned.length() < 5 || cleaned.length() > 6) {
            throw new InputsException("La placa debe tener entre 5 y 6 caracteres. Formato recibido: " + cleaned);
        }

        boolean isValidCar = cleaned.matches("^[A-Z]{3}[0-9]{3}$");
        boolean isValidMoto = cleaned.matches("^[A-Z]{3}[0-9]{2}[A-Z]$");
        boolean isValidOld = cleaned.matches("^[0-9]{3}[A-Z]{3}$");

        if (!isValidCar && !isValidMoto && !isValidOld) {
            throw new InputsException(
                    "Formato de placa inválido. Formatos válidos: ABC123 (carros), ABC12D (motos), 123ABC (antigua). Recibido: "
                            + cleaned);
        }

        return cleaned;
    }
}
