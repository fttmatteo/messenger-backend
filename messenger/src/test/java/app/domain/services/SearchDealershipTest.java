package app.domain.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import app.application.exceptions.ResourceNotFoundException;
import app.domain.model.Dealership;
import app.domain.ports.DealershipPort;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("SearchDealership Unit Tests")
class SearchDealershipTest {

    @Mock
    private DealershipPort dealershipPort;

    @InjectMocks
    private SearchDealership searchDealership;

    @Test
    @DisplayName("Debe retornar lista de todos los concesionarios")
    void shouldReturnAllDealerships() {
        List<Dealership> dealerships = Arrays.asList(new Dealership(), new Dealership());
        when(dealershipPort.findAll()).thenReturn(dealerships);

        List<Dealership> result = searchDealership.findAll();

        assertEquals(2, result.size());
        verify(dealershipPort).findAll();
    }

    @Test
    @DisplayName("Debe encontrar concesionario por ID")
    void shouldFindById() {
        Dealership d = new Dealership();
        d.setIdDealership(1L);
        when(dealershipPort.findById(1L)).thenReturn(d);

        Dealership result = searchDealership.findById(1L);

        assertEquals(1L, result.getIdDealership());
    }

    @Test
    @DisplayName("Debe lanzar excepción si ID no existe")
    void shouldThrowExceptionIfIdNotFound() {
        when(dealershipPort.findById(1L)).thenReturn(null);

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> searchDealership.findById(1L));

        assertEquals("El concesionario con ID 1 no existe.", ex.getMessage());
    }

    @Test
    @DisplayName("Debe encontrar concesionario por Nombre")
    void shouldFindByName() {
        Dealership d = new Dealership();
        d.setName("Central");
        when(dealershipPort.findByName("Central")).thenReturn(d);

        Dealership result = searchDealership.findByName("Central");

        assertEquals("Central", result.getName());
    }

    @Test
    @DisplayName("Debe lanzar excepción si Nombre no existe")
    void shouldThrowExceptionIfNameNotFound() {
        when(dealershipPort.findByName("Central")).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> searchDealership.findByName("Central"));

        assertEquals("El concesionario con nombre Central no existe.", ex.getMessage());
    }
}
