package app.adapter.in.validators;

import app.application.exceptions.InputsException;
import app.domain.model.enums.Role;

import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

/**
 * Validador de datos de entrada para empleados.
 * 
 * Esta clase extiende SimpleValidator y proporciona métodos especializados
 * para validar los datos de empleados, incluyendo reglas de seguridad para
 * contraseñas
 * y validación de roles del sistema.
 * 
 * Validaciones implementadas:
 * - documentValidator: Valida documento de identidad (máximo 10 dígitos)
 * - fullNameValidator: Valida que el nombre completo no esté vacío
 * - phoneValidator: Valida formato de teléfono (10 dígitos)
 * - userNameValidator: Valida nombre de usuario (alfanumérico, máximo 15
 * caracteres)
 * - passwordValidator: Valida contraseña segura (mínimo 8 caracteres,
 * mayúscula, número, carácter especial)
 * - roleValidator: Valida y convierte el rol a enum (ADMIN, MESSENGER)
 * 
 * Todas las validaciones lanzan InputsException con mensajes descriptivos en
 * caso de error.
 * 
 * @see SimpleValidator
 * @see app.application.exceptions.InputsException
 * @see app.domain.model.enums.Role
 * @see app.adapter.in.rest.controllers.EmployeeController
 */
@Component
public class EmployeeValidator extends SimpleValidator {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z0-9]+$");

    /** Valida documento de identidad (máximo 10 dígitos). */
    public long documentValidator(String value) throws InputsException {
        long doc = longValidator("número de cédula", value);
        if (String.valueOf(Math.abs(doc)).length() > 10) {
            throw new InputsException("la cédula no puede exceder 10 dígitos");
        }
        return doc;
    }

    /** Valida que el nombre completo no esté vacío. */
    public String fullNameValidator(String fullName) throws InputsException {
        return stringValidator("nombre completo", fullName);
    }

    /** Valida que el teléfono tenga exactamente 10 dígitos. */
    public String phoneValidator(String value) throws InputsException {
        stringValidator("número de teléfono", value);
        if (!value.matches("\\d{10}")) {
            throw new InputsException("el número de teléfono debe contener exactamente 10 dígitos");
        }
        return value;
    }

    /** Valida nombre de usuario (alfanumérico, máximo 15 caracteres). */
    public String userNameValidator(String value) throws InputsException {
        stringValidator("nombre de usuario", value);
        if (value.length() > 15) {
            throw new InputsException("el nombre de usuario no puede exceder 15 caracteres");
        }
        if (!USERNAME_PATTERN.matcher(value).matches()) {
            throw new InputsException("el nombre de usuario solo debe contener letras y números");
        }
        return value;
    }

    /**
     * Valida contraseña segura (mínimo 8 caracteres, mayúscula, número, carácter
     * especial).
     */
    public String passwordValidator(String value) throws InputsException {
        stringValidator("contraseña", value);
        if (value.length() < 8) {
            throw new InputsException("la contraseña debe contener al menos 8 caracteres");
        }
        if (!value.matches(".*[A-Z].*")) {
            throw new InputsException("la contraseña debe contener al menos una letra mayúscula");
        }
        if (!value.matches(".*[0-9].*")) {
            throw new InputsException("la contraseña debe contener al menos un número");
        }
        if (!value.matches(".*[^A-Za-z0-9].*")) {
            throw new InputsException("la contraseña debe contener al menos un carácter especial");
        }
        return value;
    }

    /** Valida y convierte el rol a enum (ADMIN o MESSENGER). */
    public Role roleValidator(String value) throws InputsException {
        stringValidator("rol", value);
        try {
            return Role.valueOf(value.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new InputsException("Rol inválido. Debe ser ADMIN o MESSENGER");
        }
    }
}
