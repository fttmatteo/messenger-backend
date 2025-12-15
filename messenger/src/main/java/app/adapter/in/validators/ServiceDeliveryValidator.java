package app.adapter.in.validators;

import org.springframework.stereotype.Component;
import app.application.exceptions.InputsException;
import app.domain.model.enums.Status;

/**
 * Validador de datos de entrada para servicios de entrega.
 * 
 * Esta clase extiende SimpleValidator y proporciona métodos especializados
 * para validar y normalizar datos de servicios de entrega, con énfasis especial
 * en la validación y normalización de placas vehiculares colombianas.
 * 
 * Validaciones implementadas:
 * - idValidator: Valida identificadores numéricos
 * - documentValidator: Valida documentos de usuario
 * - statusValidator: Valida y convierte estados de servicio a enum
 * - observationValidator: Normaliza observaciones (trim)
 * - plateNumberValidator: Valida y normaliza placas vehiculares con múltiples
 * formatos
 * 
 * Normalización de placas:
 * - Convierte a mayúsculas y elimina espacios
 * - Reemplaza caracteres ambiguos (O->0, I->1)
 * - Valida formatos: ABC123 (carros), ABC12D (motos), 123ABC (placas antiguas)
 * 
 * Todas las validaciones lanzan InputsException con mensajes descriptivos en
 * caso de error.
 * 
 * @see SimpleValidator
 * @see app.application.exceptions.InputsException
 * @see app.domain.model.enums.Status
 * @see app.adapter.in.rest.controllers.ServiceDeliveryController
 */
@Component
public class ServiceDeliveryValidator extends SimpleValidator {

    /** Valida un identificador numérico. */
    public Long idValidator(String value) throws InputsException {
        return longValidator("ID", value);
    }

    /** Valida un documento de usuario. */
    public Long documentValidator(String value) throws InputsException {
        return longValidator("documento de usuario", value);
    }

    /** Valida y convierte el estado del servicio a enum. */
    public Status statusValidator(String value) throws InputsException {
        stringValidator("estado", value);
        try {
            return Status.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InputsException(
                    "Estado inválido. Valores permitidos: ASIGNADO, ENTREGADO, PENDIENTE, FALLIDO, DEVUELTO, CANCELADO, OBSERVADO");
        }
    }

    /** Normaliza observaciones eliminando espacios en blanco. */
    public String observationValidator(String value) throws InputsException {
        if (value != null && !value.trim().isEmpty()) {
            return value.trim();
        }
        return value;
    }

    /**
     * Valida y normaliza placas vehiculares colombianas (ABC123, ABC12D, 123ABC).
     */
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
