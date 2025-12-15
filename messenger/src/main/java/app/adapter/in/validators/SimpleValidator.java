package app.adapter.in.validators;

import app.application.exceptions.InputsException;

/**
 * Clase abstracta base para todos los validadores del sistema.
 * 
 * Proporciona métodos utilitarios comunes para validación y conversión de tipos
 * básicos,
 * que son heredados y utilizados por los validadores especializados.
 * 
 * Métodos proporcionados:
 * - stringValidator: Valida que un String no sea nulo ni vacío
 * - integerValidator: Valida y convierte String a Integer
 * - longValidator: Valida y convierte String a Long
 * - doubleValidator: Valida y convierte String a Double
 * 
 * Todos los métodos lanzan InputsException con mensajes descriptivos cuando
 * la validación falla, incluyendo el nombre del elemento validado para mejor
 * experiencia de usuario.
 * 
 * Esta clase debe ser extendida por validadores específicos de dominio.
 * 
 * @see app.application.exceptions.InputsException
 * @see DealershipValidator
 * @see EmployeeValidator
 * @see ServiceDeliveryValidator
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