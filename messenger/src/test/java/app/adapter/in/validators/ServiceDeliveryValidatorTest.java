package app.adapter.in.validators;

import app.domain.exception.InputsException;
import app.domain.model.enums.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ServiceDeliveryValidator Unit Tests")
class ServiceDeliveryValidatorTest {

    private ServiceDeliveryValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ServiceDeliveryValidator();
    }

    @Nested
    @DisplayName("Validación de ID")
    class IdValidatorTests {

        @Test
        @DisplayName("Debe validar ID numérico válido")
        void shouldValidateValidId() throws InputsException {
            Long result = validator.idValidator("123");
            assertEquals(123L, result);
        }

        @Test
        @DisplayName("Debe lanzar excepción para ID nulo")
        void shouldThrowExceptionForNullId() {
            assertThrows(InputsException.class, () -> validator.idValidator(null));
        }

        @Test
        @DisplayName("Debe lanzar excepción para ID no numérico")
        void shouldThrowExceptionForNonNumericId() {
            assertThrows(InputsException.class, () -> validator.idValidator("abc"));
        }
    }

    @Nested
    @DisplayName("Validación de Documento")
    class DocumentValidatorTests {

        @Test
        @DisplayName("Debe validar documento válido")
        void shouldValidateValidDocument() throws InputsException {
            Long result = validator.documentValidator("123456789");
            assertEquals(123456789L, result);
        }

        @Test
        @DisplayName("Debe lanzar excepción para documento vacío")
        void shouldThrowExceptionForEmptyDocument() {
            assertThrows(InputsException.class, () -> validator.documentValidator(""));
        }

        @Test
        @DisplayName("Debe lanzar excepción para documento no numérico")
        void shouldThrowExceptionForNonNumericDocument() {
            assertThrows(InputsException.class, () -> validator.documentValidator("abc1234567"));
        }
    }

    @Nested
    @DisplayName("Validación de Estado")
    class StatusValidatorTests {

        @ParameterizedTest
        @ValueSource(strings = { "ASSIGNED", "DELIVERED", "PENDING", "RETURNED", "CANCELED" })
        @DisplayName("Debe validar estados válidos")
        void shouldValidateValidStatuses(String status) throws InputsException {
            Status result = validator.statusValidator(status);
            assertNotNull(result);
        }

        @Test
        @DisplayName("Debe validar estado en minúsculas")
        void shouldValidateLowercaseStatus() throws InputsException {
            Status result = validator.statusValidator("delivered");
            assertEquals(Status.DELIVERED, result);
        }

        @Test
        @DisplayName("Debe lanzar excepción para estado inválido")
        void shouldThrowExceptionForInvalidStatus() {
            InputsException exception = assertThrows(InputsException.class,
                    () -> validator.statusValidator("INVALID_STATUS"));
            assertTrue(exception.getMessage().contains("Estado inválido"));
        }

        @Test
        @DisplayName("Debe lanzar excepción para estado nulo")
        void shouldThrowExceptionForNullStatus() {
            assertThrows(InputsException.class, () -> validator.statusValidator(null));
        }
    }

    @Nested
    @DisplayName("Validación de Observación")
    class ObservationValidatorTests {

        @Test
        @DisplayName("Debe normalizar observación con espacios")
        void shouldNormalizeObservationWithSpaces() throws InputsException {
            String result = validator.observationValidator("  Observación con espacios  ");
            assertEquals("Observación con espacios", result);
        }

        @Test
        @DisplayName("Debe retornar null para observación vacía")
        void shouldReturnNullForEmptyObservation() throws InputsException {
            String result = validator.observationValidator("");
            assertEquals("", result);
        }

        @Test
        @DisplayName("Debe retornar null para observación nula")
        void shouldReturnNullForNullObservation() throws InputsException {
            String result = validator.observationValidator(null);
            assertNull(result);
        }
    }

    @Nested
    @DisplayName("Validación de Chasis")
    class ChasisNumberValidatorTests {

        @Nested
        @DisplayName("Formatos de Chasis Válidos")
        class ValidChasisTests {

            @ParameterizedTest
            @ValueSource(strings = { 
                "1HGCM82633A004123",
                "9C216260H07102143",
                "MBH1234567890",
                "VIN1234567890ABCDE"
            })
            @DisplayName("Debe validar números de chasis válidos")
            void shouldValidateValidChasis(String chasis) throws InputsException {
                String result = validator.plateNumberValidator(chasis);
                assertEquals(chasis.toUpperCase().replaceAll("[^A-Z0-9]", ""), result);
            }
        }

        @Nested
        @DisplayName("Chasis Inválidos")
        class InvalidChasisTests {

            @Test
            @DisplayName("Debe lanzar excepción para chasis nulo")
            void shouldThrowExceptionForNullChasis() {
                InputsException exception = assertThrows(InputsException.class,
                        () -> validator.plateNumberValidator(null));
                assertTrue(exception.getMessage().contains("vacío"));
            }

            @Test
            @DisplayName("Debe lanzar excepción para chasis vacío")
            void shouldThrowExceptionForEmptyChasis() {
                InputsException exception = assertThrows(InputsException.class,
                        () -> validator.plateNumberValidator(""));
                assertTrue(exception.getMessage().contains("vacío"));
            }

            @Test
            @DisplayName("Debe lanzar excepción para chasis muy corto")
            void shouldThrowExceptionForShortChasis() {
                InputsException exception = assertThrows(InputsException.class,
                        () -> validator.plateNumberValidator("ABC1234")); // 7 chars
                assertTrue(exception.getMessage().contains("entre 10 y 20"));
            }

            @Test
            @DisplayName("Debe lanzar excepción para chasis muy largo")
            void shouldThrowExceptionForLongChasis() {
                InputsException exception = assertThrows(InputsException.class,
                        () -> validator.plateNumberValidator("ABC123456789012345678901")); // 21 chars
                assertTrue(exception.getMessage().contains("entre 10 y 20"));
            }
        }
    }
}
