package app.application.usecase.route;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

import app.domain.model.Dealership;
import app.domain.model.Location;
import app.domain.model.Route;
import app.domain.ports.DealershipPort;
import app.domain.ports.LocationPort;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("CalculateOptimalRouteUseCase Unit Tests")
class CalculateOptimalRouteUseCaseTest {

    @Mock
    private LocationPort locationPort;
    @Mock
    private DealershipPort dealershipPort;

    @InjectMocks
    private CalculateOptimalRouteUseCase calculateOptimalRoute;

    @Test
    @DisplayName("Debe calcular ruta óptima con destinos válidos")
    /**
     * Verifica el cálculo de ruta óptima delegando al puerto de localización.
     */
    void shouldCalculateOptimalRouteUseCase() {
        Dealership d1 = new Dealership();
        d1.setIdDealership(1L);
        d1.setIsGeolocated(true);
        d1.setLatitude(1.0);
        d1.setLongitude(1.0);

        when(dealershipPort.findById(1L)).thenReturn(d1);

        Location origin = new Location(0.0, 0.0);
        Location dest = new Location(1.0, 1.0);
        Route expectedRoute = new Route(origin, dest, Collections.emptyList(), 1000.0, 600L, "poly");

        when(locationPort.calculateOptimalRoute(any(Location.class), anyList())).thenReturn(expectedRoute);

        Route result = calculateOptimalRoute.execute(0.0, 0.0, List.of(1L));

        assertEquals(expectedRoute, result);
    }

    @Test
    @DisplayName("Debe lanzar excepción si no hay destinos válidos")
    /**
     * Verifica validación de destinos geolocalizados antes de calcular ruta.
     */
    void shouldThrowExceptionIfNoValidDestinations() {
        Dealership d1 = new Dealership(); // Not geolocated
        d1.setIsGeolocated(false);

        when(dealershipPort.findById(1L)).thenReturn(d1);

        assertThrows(IllegalArgumentException.class, () -> calculateOptimalRoute.execute(0.0, 0.0, List.of(1L)));
    }

    @Test
    @DisplayName("Debe calcular ruta simple")
    /**
     * Verifica cálculo de ruta directa entre dos puntos.
     */
    void shouldCalculateSimpleRoute() {
        Location origin = new Location(0.0, 0.0);
        Location dest = new Location(1.0, 1.0);
        Route expected = new Route(origin, dest, Collections.emptyList(), 500.0, 300L, "poly");

        when(locationPort.calculateRoute(origin, dest)).thenReturn(expected);

        Route result = calculateOptimalRoute.calculateSimpleRoute(origin, dest);

        assertEquals(expected, result);
    }

    @Test
    @DisplayName("Debe calcular distancia")
    void shouldCalculateDistance() {
        Location origin = new Location(0.0, 0.0);
        Location dest = new Location(1.0, 1.0);

        when(locationPort.calculateDistance(origin, dest)).thenReturn(1000.0);

        Double dist = calculateOptimalRoute.calculateDistance(origin, dest);

        assertEquals(1000.0, dist);
    }
}
