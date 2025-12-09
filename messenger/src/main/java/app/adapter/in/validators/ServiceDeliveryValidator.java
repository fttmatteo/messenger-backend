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
        // Observación puede ser opcional al inicio, pero si viene, validamos formato
        // básico
        if (value != null && !value.trim().isEmpty()) {
            // Reglas extra si fuera necesario
            return value.trim();
        }
        return value;
    }
}
