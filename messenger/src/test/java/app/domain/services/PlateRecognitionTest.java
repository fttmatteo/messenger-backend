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
    @DisplayName("Placas de Carro (CAR)")
    class CarPlateTests {

        @ParameterizedTest
        @ValueSource(strings = { "ABC123", "ABC 123", "abc123", "Xyz789", "XYZ 789" })
        @DisplayName("Debe reconocer placas de carro válidas")
        /**
         * Verifica reconocimiento de patrones de placas de carro estándar.
         */
        void shouldRecognizeValidCarPlates(String plate) {
            PlateType result = plateRecognition.determinePlateType(plate);
            assertEquals(PlateType.CAR, result);
        }

        @Test
        @DisplayName("Debe reconocer placa de carro con espacios extra")
        void shouldRecognizeCarPlateWithExtraSpaces() {
            PlateType result = plateRecognition.determinePlateType("  ABC  123  ");
            assertEquals(PlateType.CAR, result);
        }
    }

    @Nested
    @DisplayName("Placas de Moto (MOTORCYCLE)")
    class MotorcyclePlateTests {

        @ParameterizedTest
        @ValueSource(strings = { "ABC12D", "ABC 12D", "abc12d", "XYZ99A", "XYZ 99A" })
        @DisplayName("Debe reconocer placas de moto válidas")
        /**
         * Verifica reconocimiento de patrones de placas de motocicleta.
         */
        void shouldRecognizeValidMotorcyclePlates(String plate) {
            PlateType result = plateRecognition.determinePlateType(plate);
            assertEquals(PlateType.MOTORCYCLE, result);
        }
    }

    @Nested
    @DisplayName("Placas de Motocarro (MOTORCAR)")
    class MotocarPlateTests {

        @ParameterizedTest
        @ValueSource(strings = { "123ABC", "123 ABC", "789xyz", "456DEF" })
        @DisplayName("Debe reconocer placas de motocarro válidas")
        void shouldRecognizeValidMotocarPlates(String plate) {
            PlateType result = plateRecognition.determinePlateType(plate);
            assertEquals(PlateType.MOTORCAR, result);
        }
    }

    @Nested
    @DisplayName("Placas Inválidas")
    class InvalidPlateTests {

        @Test
        @DisplayName("Debe lanzar excepción para placa nula")
        void shouldThrowExceptionForNullPlate() {
            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> plateRecognition.determinePlateType(null));
            assertTrue(exception.getMessage().contains("vacío"));
        }

        @Test
        @DisplayName("Debe lanzar excepción para placa vacía")
        void shouldThrowExceptionForEmptyPlate() {
            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> plateRecognition.determinePlateType(""));
            assertTrue(exception.getMessage().contains("vacío"));
        }

        @Test
        @DisplayName("Debe lanzar excepción para placa solo espacios")
        void shouldThrowExceptionForWhitespacePlate() {
            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> plateRecognition.determinePlateType("   "));
            assertTrue(exception.getMessage().contains("vacío"));
        }

        @ParameterizedTest
        @ValueSource(strings = { "AB123", "ABCD123", "AB1234", "12AB34", "ABCDEF", "123456" })
        @DisplayName("Debe lanzar excepción para formatos inválidos")
        void shouldThrowExceptionForInvalidFormats(String plate) {
            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> plateRecognition.determinePlateType(plate));
            assertTrue(exception.getMessage().contains("Formato de placa no reconocido"));
        }
    }

    @Nested
    @DisplayName("Formateo de Placas")
    class PlateFormattingTests {

        @Test
        @DisplayName("Debe formatear placa de carro correctamente")
        /**
         * Verifica el formateo estándar para almacenamiento de placas de carro.
         */
        void shouldFormatCarPlate() {
            String result = plateRecognition.formatPlateForStorage("ABC123", PlateType.CAR);
            assertEquals("ABC 123", result);
        }

        @Test
        @DisplayName("Debe formatear placa de moto correctamente")
        void shouldFormatMotorcyclePlate() {
            String result = plateRecognition.formatPlateForStorage("ABC12D", PlateType.MOTORCYCLE);
            assertEquals("ABC 12D", result);
        }

        @Test
        @DisplayName("Debe formatear placa de motocarro correctamente")
        void shouldFormatMotocarPlate() {
            String result = plateRecognition.formatPlateForStorage("123ABC", PlateType.MOTORCAR);
            assertEquals("123 ABC", result);
        }

        @Test
        @DisplayName("Debe normalizar a mayúsculas al formatear")
        void shouldNormalizeToUppercase() {
            String result = plateRecognition.formatPlateForStorage("abc123", PlateType.CAR);
            assertEquals("ABC 123", result);
        }

        @Test
        @DisplayName("Debe remover espacios extra al formatear")
        void shouldRemoveExtraSpaces() {
            String result = plateRecognition.formatPlateForStorage("ABC  123", PlateType.CAR);
            assertEquals("ABC 123", result);
        }
    }
}
