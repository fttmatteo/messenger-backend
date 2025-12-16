package app.application.usecase;

import app.application.exceptions.BusinessException;
import app.application.exceptions.ResourceNotFoundException;
import app.domain.model.Dealership;
import app.domain.services.CreateDealership;
import app.domain.services.DeleteDealership;
import app.domain.services.SearchDealership;
import app.domain.services.UpdateDealership;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para DealershipUseCase.
 * 
 * Verifica la orquestación correcta de operaciones CRUD de concesionarios.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DealershipUseCase Unit Tests")
class DealershipUseCaseTest {

    @Mock
    private CreateDealership createDealership;

    @Mock
    private SearchDealership searchDealership;

    @Mock
    private UpdateDealership updateDealership;

    @Mock
    private DeleteDealership deleteDealership;

    @InjectMocks
    private DealershipUseCase dealershipUseCase;

    private Dealership sampleDealership;

    @BeforeEach
    void setUp() {
        sampleDealership = new Dealership();
        sampleDealership.setIdDealership(1L);
        sampleDealership.setName("Concesionario Test");
        sampleDealership.setAddress("Calle 123 #45-67");
        sampleDealership.setZone("Zona Norte");
    }

    @Nested
    @DisplayName("Crear Concesionario")
    class CreateTests {

        @Test
        @DisplayName("Debe crear concesionario exitosamente")
        void shouldCreateDealershipSuccessfully() throws Exception {
            dealershipUseCase.create(sampleDealership);

            verify(createDealership, times(1)).create(sampleDealership);
        }

        @Test
        @DisplayName("Debe propagar excepción si creación falla")
        void shouldPropagateExceptionOnCreateFailure() throws Exception {
            doThrow(new BusinessException("Nombre duplicado"))
                    .when(createDealership).create(any());

            assertThrows(BusinessException.class,
                    () -> dealershipUseCase.create(sampleDealership));
        }
    }

    @Nested
    @DisplayName("Buscar Concesionarios")
    class SearchTests {

        @Test
        @DisplayName("Debe retornar todos los concesionarios")
        void shouldReturnAllDealerships() {
            Dealership dealership2 = new Dealership();
            dealership2.setIdDealership(2L);
            dealership2.setName("Otro Concesionario");

            when(searchDealership.findAll()).thenReturn(List.of(sampleDealership, dealership2));

            List<Dealership> result = dealershipUseCase.findAll();

            assertEquals(2, result.size());
            verify(searchDealership).findAll();
        }

        @Test
        @DisplayName("Debe retornar lista vacía si no hay concesionarios")
        void shouldReturnEmptyListIfNoDealerships() {
            when(searchDealership.findAll()).thenReturn(List.of());

            List<Dealership> result = dealershipUseCase.findAll();

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Debe buscar concesionario por ID")
        void shouldFindDealershipById() throws Exception {
            when(searchDealership.findById(1L)).thenReturn(sampleDealership);

            Dealership result = dealershipUseCase.findById(1L);

            assertNotNull(result);
            assertEquals("Concesionario Test", result.getName());
        }

        @Test
        @DisplayName("Debe lanzar excepción si ID no existe")
        void shouldThrowExceptionIfIdNotFound() {
            when(searchDealership.findById(999L))
                    .thenThrow(new ResourceNotFoundException("No encontrado"));

            assertThrows(ResourceNotFoundException.class,
                    () -> dealershipUseCase.findById(999L));
        }

        @Test
        @DisplayName("Debe buscar concesionario por nombre")
        void shouldFindDealershipByName() throws Exception {
            when(searchDealership.findByName("Concesionario Test")).thenReturn(sampleDealership);

            Dealership result = dealershipUseCase.findByName("Concesionario Test");

            assertNotNull(result);
            assertEquals("Concesionario Test", result.getName());
        }
    }

    @Nested
    @DisplayName("Actualizar Concesionario")
    class UpdateTests {

        @Test
        @DisplayName("Debe actualizar concesionario exitosamente")
        void shouldUpdateDealershipSuccessfully() throws Exception {
            sampleDealership.setAddress("Nueva dirección");

            dealershipUseCase.update(1L, sampleDealership);

            verify(updateDealership, times(1)).update(1L, sampleDealership);
        }

        @Test
        @DisplayName("Debe propagar excepción si actualización falla")
        void shouldPropagateExceptionOnUpdateFailure() throws Exception {
            doThrow(new BusinessException("Concesionario no existe"))
                    .when(updateDealership).update(anyLong(), any());

            assertThrows(BusinessException.class,
                    () -> dealershipUseCase.update(1L, sampleDealership));
        }
    }

    @Nested
    @DisplayName("Eliminar Concesionario")
    class DeleteTests {

        @Test
        @DisplayName("Debe eliminar concesionario por ID sin servicios")
        void shouldDeleteDealershipByIdWithoutServices() throws Exception {
            dealershipUseCase.deleteById(1L);

            verify(deleteDealership, times(1)).deleteById(1L);
        }

        @Test
        @DisplayName("Debe eliminar concesionario por nombre sin servicios")
        void shouldDeleteDealershipByNameWithoutServices() throws Exception {
            dealershipUseCase.deleteByName("Concesionario Test");

            verify(deleteDealership, times(1)).deleteByName("Concesionario Test");
        }

        @Test
        @DisplayName("Debe lanzar excepción si tiene servicios activos")
        void shouldThrowExceptionIfHasActiveServices() throws Exception {
            doThrow(new BusinessException("Tiene servicios activos"))
                    .when(deleteDealership).deleteById(1L);

            assertThrows(BusinessException.class,
                    () -> dealershipUseCase.deleteById(1L));
        }
    }
}
