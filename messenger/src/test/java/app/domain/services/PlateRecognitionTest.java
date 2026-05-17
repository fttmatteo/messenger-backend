package app.domain.services;

import app.domain.exception.BusinessException;
import app.domain.model.enums.PlateType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PlateRecognition Unit Tests")
class PlateRecognitionTest {

    private PlateRecognition plateRecognition;

    @BeforeEach
    void setUp() {
        plateRecognition = new PlateRecognition();
    }

    @Nested
    @DisplayName("Validación de Chasis")
    class ChasisTests {

        @ParameterizedTest
        @ValueSource(strings = { 
            "1HGCM82633A004123", 
            "ABC12345674567890", 
            "MBH9876543210", 
            "CHASIS123456789" 
        })
        @DisplayName("Debe reconocer seriales de chasis válidos como MOTORCYCLE")
        void shouldRecognizeValidChasis(String chasis) {
            PlateType result = plateRecognition.determinePlateType(chasis);
            assertEquals(PlateType.MOTORCYCLE, result);
        }

        @Test
        @DisplayName("Debe limpiar espacios al validar chasis")
        void shouldCleanSpacesInChasis() {
            PlateType result = plateRecognition.determinePlateType("  ABC 1234567890  ");
            assertEquals(PlateType.MOTORCYCLE, result);
        }
    }

    @Nested
    @DisplayName("Chasis Inválidos")
    class InvalidChasisTests {

        @Test
        @DisplayName("Debe lanzar excepción para chasis nulo")
        void shouldThrowExceptionForNullChasis() {
            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> plateRecognition.determinePlateType(null));
            assertTrue(exception.getMessage().contains("vacío"));
        }

        @ParameterizedTest
        @ValueSource(strings = { "ABC123", "12345", "ABCDE1234567890123456" })
        @DisplayName("Debe lanzar excepción para formatos inválidos")
        void shouldThrowExceptionForInvalidFormats(String chasis) {
            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> plateRecognition.determinePlateType(chasis));
            assertTrue(exception.getMessage().contains("longitud válida"));
        }
    }

    @Nested
    @DisplayName("Formateo de Chasis")
    class ChasisFormattingTests {

        @Test
        @DisplayName("Debe formatear chasis correctamente (sin espacios y en mayúsculas)")
        void shouldFormatChasis() {
            String result = plateRecognition.formatPlateForStorage("abc 123 4567890", PlateType.MOTORCYCLE);
            assertEquals("ABC1234567890", result);
        }
    }
}
