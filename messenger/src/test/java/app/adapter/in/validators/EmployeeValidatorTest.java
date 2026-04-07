package app.adapter.in.validators;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import app.domain.exception.InputsException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pruebas unitarias de Caja Negra para EmployeeValidator.
 * Cubre las clases de equivalencia inválidas y valores límite
 * para phoneValidator y passwordValidator.
 */
@DisplayName("EmployeeValidator - Pruebas de Caja Negra")
class EmployeeValidatorTest {

    private final EmployeeValidator employeeValidator = new EmployeeValidator();

    // phoneValidator

    @Test
    @DisplayName("Teléfono válido de 10 dígitos no lanza excepción")
    void testPhoneValid() {
        assertDoesNotThrow(() -> employeeValidator.phoneValidator("3001234567"));
    }

    @Test
    @DisplayName("Teléfono con menos de 10 dígitos lanza InputsException")
    void testPhoneInvalid_TooShort() {
        assertThrows(InputsException.class,
                () -> employeeValidator.phoneValidator("123456789"));
    }

    @Test
    @DisplayName("Teléfono con caracteres alfabéticos lanza InputsException")
    void testPhoneInvalid_Alphabetic() {
        assertThrows(InputsException.class,
                () -> employeeValidator.phoneValidator("abcdefghij"));
    }

    @Test
    @DisplayName("Teléfono con mezcla de dígitos y letras lanza InputsException")
    void testPhoneInvalid_Mixed() {
        assertThrows(InputsException.class,
                () -> employeeValidator.phoneValidator("300123456A"));
    }

    @Test
    @DisplayName("Teléfono con más de 10 dígitos lanza InputsException")
    void testPhoneInvalid_TooLong() {
        assertThrows(InputsException.class,
                () -> employeeValidator.phoneValidator("30012345678"));
    }

    @Test
    @DisplayName("Teléfono nulo lanza InputsException")
    void testPhoneInvalid_Null() {
        assertThrows(InputsException.class,
                () -> employeeValidator.phoneValidator(null));
    }

    @Test
    @DisplayName("Teléfono vacío lanza InputsException")
    void testPhoneInvalid_Empty() {
        assertThrows(InputsException.class,
                () -> employeeValidator.phoneValidator(""));
    }

    // passwordValidator

    @Test
    @DisplayName("Contraseña válida no lanza excepción")
    void testPasswordValid() {
        assertDoesNotThrow(() -> employeeValidator.passwordValidator("SeguraPlak2026*"));
    }

    @Test
    @DisplayName("Contraseña con menos de 8 caracteres lanza InputsException")
    void testPasswordInvalid_TooShort() {
        assertThrows(InputsException.class,
                () -> employeeValidator.passwordValidator("Plak26*"));
    }

    @Test
    @DisplayName("Contraseña sin mayúsculas lanza InputsException")
    void testPasswordInvalid_NoUppercase() {
        assertThrows(InputsException.class,
                () -> employeeValidator.passwordValidator("seguraplak2026*"));
    }

    @Test
    @DisplayName("Contraseña sin números lanza InputsException")
    void testPasswordInvalid_NoNumber() {
        assertThrows(InputsException.class,
                () -> employeeValidator.passwordValidator("SeguraPlak*"));
    }

    @Test
    @DisplayName("Contraseña sin carácter especial lanza InputsException")
    void testPasswordInvalid_NoSpecialChar() {
        assertThrows(InputsException.class,
                () -> employeeValidator.passwordValidator("SeguraPlak2026"));
    }

    @Test
    @DisplayName("Contraseña nula lanza InputsException")
    void testPasswordInvalid_Null() {
        assertThrows(InputsException.class,
                () -> employeeValidator.passwordValidator(null));
    }

    @Test
    @DisplayName("Contraseña vacía lanza InputsException")
    void testPasswordInvalid_Empty() {
        assertThrows(InputsException.class,
                () -> employeeValidator.passwordValidator(""));
    }

    // documentValidator

    @Test
    @DisplayName("Documento válido no lanza excepción")
    void testDocumentValid() {
        assertDoesNotThrow(() -> employeeValidator.documentValidator("1234567890"));
    }

    @Test
    @DisplayName("Documento con más de 10 dígitos lanza InputsException")
    void testDocumentInvalid_TooLong() {
        assertThrows(InputsException.class,
                () -> employeeValidator.documentValidator("12345678901"));
    }

    @Test
    @DisplayName("Documento con letras lanza InputsException")
    void testDocumentInvalid_NonNumeric() {
        assertThrows(InputsException.class,
                () -> employeeValidator.documentValidator("abc"));
    }

    @Test
    @DisplayName("Documento nulo lanza InputsException")
    void testDocumentInvalid_Null() {
        assertThrows(InputsException.class,
                () -> employeeValidator.documentValidator(null));
    }

    // fullNameValidator 

    @Test
    @DisplayName("Nombre completo válido no lanza excepción")
    void testFullNameValid() {
        assertDoesNotThrow(() -> employeeValidator.fullNameValidator("Juan Pérez"));
    }

    @Test
    @DisplayName("Nombre completo nulo lanza InputsException")
    void testFullNameInvalid_Null() {
        assertThrows(InputsException.class,
                () -> employeeValidator.fullNameValidator(null));
    }

    // roleValidator

    @Test
    @DisplayName("Rol válido ADMIN no lanza excepción")
    void testRoleValid() {
        assertDoesNotThrow(() -> employeeValidator.roleValidator("ADMIN"));
    }

    @Test
    @DisplayName("Rol inválido lanza InputsException")
    void testRoleInvalid() {
        assertThrows(InputsException.class,
                () -> employeeValidator.roleValidator("GERENTE"));
    }

    @Test
    @DisplayName("Rol nulo lanza InputsException")
    void testRoleInvalid_Null() {
        assertThrows(InputsException.class,
                () -> employeeValidator.roleValidator(null));
    }
}
