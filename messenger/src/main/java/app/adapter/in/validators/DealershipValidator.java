package app.adapter.in.validators;

import org.springframework.stereotype.Component;
import app.application.exceptions.InputsException;

/**
 * Validador de datos de entrada para concesionarios.
 * 
 * Esta clase extiende SimpleValidator y proporciona métodos especializados
 * para validar los datos de concesionarios antes de procesarlos en la capa de
 * aplicación.
 * 
 * Validaciones implementadas:
 * - nameValidator: Valida que el nombre no esté vacío
 * - addressValidator: Valida que la dirección no esté vacía
 * - phoneValidator: Valida formato de teléfono (10 dígitos)
 * - zoneValidator: Valida que la zona sea "centro", "sur" o "norte"
 * 
 * Todas las validaciones lanzan InputsException con mensajes descriptivos en
 * caso de error.
 * 
 * @see SimpleValidator
 * @see app.application.exceptions.InputsException
 * @see app.adapter.in.rest.controllers.DealershipController
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