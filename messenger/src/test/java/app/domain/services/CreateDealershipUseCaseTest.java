package app.domain.services;
import app.application.usecase.dealership.CreateDealershipUseCase;

import app.domain.exception.BusinessException;
import app.domain.model.Dealership;
import app.domain.ports.DealershipPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas unitarias de CreateDealershipUseCase")
class CreateDealershipTest {

    @Mock
    private DealershipPort dealershipPort;

    @InjectMocks
    private CreateDealershipUseCase createDealership;

    private Dealership newDealership;

    @BeforeEach
    void setUp() {
        newDealership = new Dealership();
        newDealership.setName("Concesionario Test");
        newDealership.setAddress("Calle 123 #45-67");
        newDealership.setPhone("3001234567");
        newDealership.setZone("Zona Norte");
    }

    @Nested
    @DisplayName("Creación Exitosa")
    class SuccessfulCreationTests {

        @Test
        @DisplayName("Debe crear concesionario cuando nombre es único")
        void shouldCreateDealershipWhenNameIsUnique() throws Exception {
            when(dealershipPort.findByName("Concesionario Test")).thenReturn(null);
            when(dealershipPort.save(newDealership)).thenReturn(newDealership);

            createDealership.create(newDealership);

            verify(dealershipPort).save(newDealership);
        }

        @Test
        @DisplayName("Debe buscar por nombre antes de crear")

        void shouldSearchByNameBeforeCreating() throws Exception {
            when(dealershipPort.findByName(anyString())).thenReturn(null);
            when(dealershipPort.save(any())).thenReturn(newDealership);

            createDealership.create(newDealership);

            verify(dealershipPort).findByName("Concesionario Test");
            verify(dealershipPort).save(any());
        }
    }

    @Nested
    @DisplayName("Validación de Nombre")
    class NameValidationTests {

        @Test
        @DisplayName("Debe lanzar excepción si nombre ya existe")
        void shouldThrowExceptionIfNameExists() {
            Dealership existingDealership = new Dealership();
            existingDealership.setName("Concesionario Test");
            when(dealershipPort.findByName("Concesionario Test")).thenReturn(existingDealership);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> createDealership.create(newDealership));

            assertEquals("Ya existe un concesionario con ese nombre.", exception.getMessage());
            verify(dealershipPort, never()).save(any());
        }
    }
}
