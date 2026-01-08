package app.application.usecase.route;

import app.domain.model.Dealership;
import app.domain.model.Location;
import app.domain.model.Route;
import app.domain.ports.DealershipPort;
import app.domain.ports.LocationPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Caso de uso para calcular rutas óptimas entre concesionarios.
 */
@Service
public class CalculateOptimalRouteUseCase {

    private static final Logger logger = LoggerFactory.getLogger(CalculateOptimalRouteUseCase.class);

    @Autowired
    private LocationPort locationPort;
    @Autowired
    private DealershipPort dealershipPort;

    /**
     * Calcula la ruta óptima desde un origen visitando múltiples concesionarios.
     */
    public Route execute(Double originLat, Double originLng, List<Long> dealershipIds) {
        // ...existing code...

        Location origin = new Location(originLat, originLng);

        List<Location> destinations = new ArrayList<>();
        for (Long id : dealershipIds) {
            Dealership dealership = dealershipPort.findById(id);
            if (dealership.getIsGeolocated() != null && dealership.getIsGeolocated()) {
                destinations.add(dealership.getLocation());
            } else {
                logger.warn("Concesionario ID: {} ignorado en ruta por falta de geolocalización", id);
            }
        }

        if (destinations.isEmpty()) {
            logger.error("No se encontraron destinos válidos para la ruta");
            throw new IllegalArgumentException("No hay concesionarios geocodificados para calcular la ruta");
        }

        return locationPort.calculateOptimalRoute(origin, destinations);
    }

    /**
     * Calcula una ruta simple entre dos puntos.
     */
    public Route calculateSimpleRoute(Location origin, Location destination) {
        return locationPort.calculateRoute(origin, destination);
    }

    /**
     * Calcula la distancia entre dos ubicaciones.
     */
    public Double calculateDistance(Location from, Location to) {
        return locationPort.calculateDistance(from, to);
    }
}
