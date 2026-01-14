package app.application.usecase.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import app.domain.exception.GeolocationException;
import app.domain.model.Dealership;
import app.domain.model.Location;
import app.domain.ports.DealershipPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("ValidateDeliveryLocationUseCase Unit Tests")
class ValidateDeliveryLocationUseCaseTest {

    @Mock
    private DealershipPort dealershipPort;

    @InjectMocks
    private ValidateDeliveryLocationUseCase validateDeliveryLocation;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(validateDeliveryLocation, "maxDistanceMeters", 200.0);
    }

    @Test
    @DisplayName("Debe permitir si concesionario no está geolocalizado")
    /**
     * Verifica que la validación pase si el concesionario destino no tiene
     * coordenadas.
     */
    void shouldAllowIfDealershipNotGeolocated() {
        Dealership d = new Dealership();
        d.setIsGeolocated(false);
        when(dealershipPort.findById(1L)).thenReturn(d);

        boolean result = validateDeliveryLocation.execute(new Location(0.0, 0.0), 1L);

        assertTrue(result);
    }

    @Test
    @DisplayName("Debe permitir si está dentro del rango")
    /**
     * Verifica que la validación pase si la entrega está cerca del concesionario.
     */
    void shouldAllowIfWithinRange() {
        Dealership d = new Dealership();
        d.setIsGeolocated(true);
        d.setLatitude(4.0);
        d.setLongitude(-72.0);
        when(dealershipPort.findById(1L)).thenReturn(d);

        boolean result = validateDeliveryLocation.execute(new Location(4.0, -72.0), 1L);

        assertTrue(result);
    }

    @Test
    @DisplayName("Debe lanzar excepción si está fuera de rango")
    /**
     * Verifica que se lance GeolocationException si la entrega es muy lejos del
     * destino.
     */
    void shouldThrowIfOutOfRange() {
        Dealership d = new Dealership();
        d.setIsGeolocated(true);
        d.setLatitude(4.0);
        d.setLongitude(-72.0);
        d.setName("Dealership 1");
        when(dealershipPort.findById(1L)).thenReturn(d);

        Location deliveryLoc = new Location(5.0, -73.0);

        assertThrows(GeolocationException.class, () -> validateDeliveryLocation.execute(deliveryLoc, 1L));
    }

    @Test
    @DisplayName("isWithinRange debe retornar false si está fuera de rango")
    void isWithinRangeShouldReturnFalseIfFar() {
        Dealership d = new Dealership();
        d.setIsGeolocated(true);
        d.setLatitude(4.0);
        d.setLongitude(-72.0);
        when(dealershipPort.findById(1L)).thenReturn(d);

        Location deliveryLoc = new Location(5.0, -73.0);

        assertFalse(validateDeliveryLocation.isWithinRange(deliveryLoc, 1L));
    }

    @Test
    @DisplayName("isWithinRange debe retornar true si es válido")
    void isWithinRangeShouldReturnTrueIfValid() {
        Dealership d = new Dealership();
        d.setIsGeolocated(true);
        d.setLatitude(4.0);
        d.setLongitude(-72.0);
        when(dealershipPort.findById(1L)).thenReturn(d);

        assertTrue(validateDeliveryLocation.isWithinRange(new Location(4.0, -72.0), 1L));
    }
}
