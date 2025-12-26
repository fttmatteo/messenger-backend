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
        /**
         * Verifica que un string numérico válido sea aceptado como ID.
         */
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
        /**
         * Verifica validación de formato de documento de identidad.
         */
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
            assertThrows(InputsException.class, () -> validator.documentValidator("abc123"));
        }
    }

    @Nested
    @DisplayName("Validación de Estado")
    class StatusValidatorTests {

        @ParameterizedTest
        @ValueSource(strings = { "ASSIGNED", "DELIVERED", "PENDING", "RETURNED", "CANCELED" })
        @DisplayName("Debe validar estados válidos")
        /**
         * Verifica que los estados de negocio permitidos sean aceptados.
         */
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
    @DisplayName("Validación de Placas")
    class PlateNumberValidatorTests {

        @Nested
        @DisplayName("Placas de Carro (ABC123)")
        class CarPlateTests {

            @ParameterizedTest
            @ValueSource(strings = { "ABC123", "abc123", "AbC123", "XYZ789" })
            @DisplayName("Debe validar placas de carro válidas")
            /**
             * Verifica formatos válidos para placas de automóviles.
             */
            void shouldValidateValidCarPlates(String plate) throws InputsException {
                String result = validator.plateNumberValidator(plate);
                assertTrue(result.matches("^[A-Z]{3}[0-9]{3}$"));
            }
        }

        @Nested
        @DisplayName("Placas de Moto (ABC12D)")
        class MotoPlateTests {

            @ParameterizedTest
            @ValueSource(strings = { "ABC12D", "abc12d", "XYZ99A" })
            @DisplayName("Debe validar placas de moto válidas")
            void shouldValidateValidMotoPlates(String plate) throws InputsException {
                String result = validator.plateNumberValidator(plate);
                assertTrue(result.matches("^[A-Z]{3}[0-9]{2}[A-Z]$"));
            }
        }

        @Nested
        @DisplayName("Placas Antiguas (123ABC)")
        class OldPlateTests {

            @ParameterizedTest
            @ValueSource(strings = { "123ABC", "456XYZ", "789DEF" })
            @DisplayName("Debe validar placas antiguas válidas")
            void shouldValidateValidOldPlates(String plate) throws InputsException {
                String result = validator.plateNumberValidator(plate);
                assertTrue(result.matches("^[0-9]{3}[A-Z]{3}$"));
            }
        }

        @Nested
        @DisplayName("Normalización de Caracteres Ambiguos")
        class AmbiguousCharacterTests {

            @Test
            @DisplayName("Debe reemplazar O por 0")
            void shouldReplaceOWithZero() throws InputsException {
                String result = validator.plateNumberValidator("ABC1O3");
                assertEquals("ABC103", result);
            }

            @Test
            @DisplayName("Debe reemplazar I por 1")
            void shouldReplaceIWithOne() throws InputsException {
                String result = validator.plateNumberValidator("ABCI23");
                assertEquals("ABC123", result);
            }
        }

        @Nested
        @DisplayName("Placas Inválidas")
        class InvalidPlateTests {

            @Test
            @DisplayName("Debe lanzar excepción para placa nula")
            void shouldThrowExceptionForNullPlate() {
                InputsException exception = assertThrows(InputsException.class,
                        () -> validator.plateNumberValidator(null));
                assertTrue(exception.getMessage().contains("vacío"));
            }

            @Test
            @DisplayName("Debe lanzar excepción para placa vacía")
            void shouldThrowExceptionForEmptyPlate() {
                InputsException exception = assertThrows(InputsException.class,
                        () -> validator.plateNumberValidator(""));
                assertTrue(exception.getMessage().contains("vacío"));
            }

            @Test
            @DisplayName("Debe lanzar excepción para placa muy corta")
            void shouldThrowExceptionForShortPlate() {
                InputsException exception = assertThrows(InputsException.class,
                        () -> validator.plateNumberValidator("AB12"));
                assertTrue(exception.getMessage().contains("6 caracteres"));
            }

            @Test
            @DisplayName("Debe lanzar excepción para placa muy larga")
            void shouldThrowExceptionForLongPlate() {
                InputsException exception = assertThrows(InputsException.class,
                        () -> validator.plateNumberValidator("ABC1234"));
                assertTrue(exception.getMessage().contains("6 caracteres"));
            }

            @Test
            @DisplayName("Debe lanzar excepción para formato inválido")
            void shouldThrowExceptionForInvalidFormat() {
                InputsException exception = assertThrows(InputsException.class,
                        () -> validator.plateNumberValidator("12AB34"));
                assertTrue(exception.getMessage().contains("Formato de placa inválido"));
            }
        }
    }
}
